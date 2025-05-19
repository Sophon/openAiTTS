package org.example.openaitts.feature.realtimeAgent.data

import android.content.Context
import android.util.Log
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.example.openaitts.feature.realtimeAgent.domain.OpenAiEvent
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.audio.JavaAudioDeviceModule
import java.net.URL
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicReference
import javax.net.ssl.HttpsURLConnection
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class WebRTCClient (
    onIncomingEvent: (OpenAiEvent) -> Unit,
    context: Context,
) {
    private val peerConnectionFactory: PeerConnectionFactory
    private val peerConnection: PeerConnection
    private val negotiateJob = AtomicReference<Job?>(null)
    private val audioSource: AudioSource
    private val localAudioTrack: AudioTrack
    private val dataChannel: DataChannel

    init {
        peerConnectionFactory = createPeerConnectionFactory(context)
        peerConnection = createPeerConnection()

        createAudioSourceAndTrack().let {
            audioSource = it.first
            localAudioTrack = it.second
        }

        val sender = peerConnection.addTrack(localAudioTrack)
        if (sender == null) {
            Napier.e(tag = TAG) { "Adding audio track: failed" }
        } else {
            Napier.d(tag = TAG) { "Adding audio track: success" }
        }

        dataChannel = peerConnection.createDataChannel("oai-events", DataChannel.Init())
        dataChannel.registerObserver(
            object : DataChannel.Observer {
                override fun onBufferedAmountChange(p0: Long) {}

                override fun onStateChange() {}

                override fun onMessage(buffer: DataChannel.Buffer) {
                    try {
                        val bytes = ByteArray(buffer.data.remaining())
                        buffer.data.get(bytes)
                        val message = String(bytes, Charsets.UTF_8)
                        Napier.d(tag = TAG) { "Received message: $message" }
                        val decoded = JSON_INSTANCE.decodeFromString<OpenAiEvent>(message)
                        onIncomingEvent(decoded)
                    } catch (e: Exception) {
                        Napier.d(tag = TAG) { "Failed to parse incoming event: $e" }
                    }
                }
            }
        )
    }

    suspend fun dispose() {
        try {
            negotiateJob.get()?.cancelAndJoin()
            localAudioTrack.dispose()
            audioSource.dispose()
            dataChannel.dispose()
            peerConnection.close()
            peerConnectionFactory.dispose()
        } catch (e: Exception) {
            Log.e(TAG, "Error disposing resources", e)
        }
    }

    fun setAudioTrackEnabled(enabled: Boolean) {
        localAudioTrack.setEnabled(enabled)
    }

    fun isAudioTrackEnabled(): Boolean = localAudioTrack.enabled()

    fun <T> sendDataMessage(serializer: KSerializer<T>, msg: T) {
        val msgString = JSON_INSTANCE.encodeToString(serializer, msg)

        dataChannel.send(
            DataChannel.Buffer(
                ByteBuffer.wrap(msgString.toByteArray(charset = Charsets.UTF_8)),
                false
            )
        )
    }

    suspend fun negotiateConnection(
        baseUrl: String,
        apiKey: String,
        model: String,
    ) {
        val job = coroutineScope {
            launch {
                try {
                    val offer = createOffer()
                    Napier.d(tag = TAG) { "Offer: created; SDP = ${offer.description}" }

                    setLocalDescription(offer)
                    Napier.d(tag = TAG) { "Local description set" }

                    val answerSdp = withContext(Dispatchers.IO) {
                        ensureActive()

                        val url = URL("$baseUrl?model=$model")
                        val connection = url.openConnection() as HttpsURLConnection
                        connection.apply {
                            requestMethod = "POST"
                            setRequestProperty("Authorization", "Bearer $apiKey")
                            setRequestProperty("Content-Type", "application/sdp")
                            doOutput = true
                        }

                        connection.outputStream.use { os -> os.write(offer.description.toByteArray()) }

                        val responseCode = connection.responseCode
                        if (responseCode < 200 || responseCode > 299) {
                            // Read error response body
                            val errorBody =
                                connection.errorStream?.bufferedReader()?.use { it.readText() }
                                    ?: "No error body"
                            Napier.d(tag = TAG) { "Server responded with code $responseCode: $errorBody" }
                            throw Exception("HTTP error: $responseCode, body: $errorBody")
                        }

                        connection.inputStream.bufferedReader().use { it.readText() }
                    }

                    ensureActive()

                    Napier.d(tag = TAG) { "Received SDP answer: $answerSdp" }

                    val answer = SessionDescription(SessionDescription.Type.ANSWER, answerSdp)
                    setRemoteDescription(answer)
                    Napier.d(tag = TAG) { "Remote description set" }
                } catch (e: Exception) {
                    Napier.e(tag = TAG) { "Failed to connect to LLM: $e" }
                    throw Exception("Failed to connect to LLM: $e")
                } finally {
                    negotiateJob.set(null)
                }
            }
        }

        if (negotiateJob.compareAndSet(null, job).not()) {
            throw Exception("negotiateConnection already in progress")
        }
    }

    private fun createPeerConnectionFactory(context: Context): PeerConnectionFactory {
        Napier.d(tag = TAG) { "PeerConnectionFactory: initializing" }
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        val audioDeviceModule = JavaAudioDeviceModule.builder(context)
            .createAudioDeviceModule()

        val options = PeerConnectionFactory.Options()

        val pcFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setAudioDeviceModule(audioDeviceModule)
            .createPeerConnectionFactory()
        Napier.d(tag = TAG) { "PeerConnectionFactory: initialized" }

        return pcFactory
    }

    private fun createPeerConnection(): PeerConnection {
        Napier.d(tag = TAG) { "PeerConnection: creating" }

        val iceServers = ArrayList<PeerConnection.IceServer>()
        iceServers.add(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN

        val pc = peerConnectionFactory.createPeerConnection(rtcConfig, LoggingPeerConnectionObserver)
            ?: throw IllegalStateException("Failed to create PeerConnection")

        Napier.d(tag = TAG) { "PeerConnection: created" }

        return pc
    }

    private fun createAudioSourceAndTrack(): Pair<AudioSource, AudioTrack> {
        Napier.d(tag = TAG) { "Audio track: creating and adding" }
        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        val localAudioTrack = peerConnectionFactory.createAudioTrack("mic", audioSource)
        localAudioTrack.setEnabled(true)

        return Pair(audioSource, localAudioTrack)
    }

    private fun createOffer(sdpObserver: SdpObserver) {
        Napier.d(tag = TAG) { "creating offer" }

        val constraints = MediaConstraints()
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        constraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))

        peerConnection.createOffer(sdpObserver, constraints)
    }

    private suspend fun createOffer(): SessionDescription {
        return suspendCancellableCoroutine { continuation ->
            createOffer(
                object : SdpObserver {
                    override fun onCreateSuccess(sessionDescription: SessionDescription) {
                        Napier.d(tag = TAG) { "Offer created successfully" }
                        continuation.resume(sessionDescription)
                    }

                    override fun onCreateFailure(s: String) {
                        Napier.d(tag = TAG) { "Failed to create offer $s" }
                        continuation.resumeWithException(Exception("Create offer failed: $s"))
                    }

                    override fun onSetSuccess() {}
                    override fun onSetFailure(s: String) {}
                }
            )
        }
    }


    private suspend fun setLocalDescription(sessionDescription: SessionDescription) {
        suspendCancellableCoroutine { continuation ->
            peerConnection.setRemoteDescription(
                object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onCreateFailure(p0: String?) {}

                    override fun onSetSuccess() {
                        Napier.d(tag = TAG) { "Remote: description set successfully" }
                        continuation.resume(Unit)
                    }

                    override fun onSetFailure(p0: String?) {
                        Napier.e(tag = TAG) { "Remote: description set failed: $p0" }
                    }
                },
                sessionDescription,
            )
        }
    }

    private suspend fun setRemoteDescription(sessionDescription: SessionDescription) {
        suspendCancellableCoroutine { continuation ->
            peerConnection.setRemoteDescription(
                object : SdpObserver {
                    override fun onCreateSuccess(sessionDescription: SessionDescription) {}
                    override fun onCreateFailure(s: String) {}
                    override fun onSetSuccess() {
                        Napier.d(tag = TAG) { "Remote description set successfully" }
                        continuation.resume(Unit)
                    }

                    override fun onSetFailure(s: String) {
                        Napier.e(tag = TAG) { "Failed to set remote description: $s" }
                        continuation.resumeWithException(Exception("Set remote description failed: $s"))
                    }
                },
                sessionDescription
            )
        }
    }

    private object LoggingPeerConnectionObserver: PeerConnection.Observer {
        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
            Napier.d(tag = TAG) { "onSignalingChange: $p0" }
        }

        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
            Napier.d(tag = TAG) { "onIceConnectionChange: $p0" }
        }

        override fun onIceConnectionReceivingChange(p0: Boolean) {
            Napier.d(tag = TAG) { "onIceConnectionReceivingChange: $p0" }
        }

        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
            Napier.d(tag = TAG) { "onIceGatheringChange: $p0" }
        }

        override fun onIceCandidate(p0: IceCandidate?) {
            Napier.d(tag = TAG) { "onIceCandidate: $p0" }
        }

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
            Napier.d(tag = TAG) { "onIceCandidatesRemoved: $p0" }
        }

        override fun onAddStream(p0: MediaStream?) {
            Napier.d(tag = TAG) { "onAddStream: $p0" }
        }

        override fun onRemoveStream(p0: MediaStream?) {
            Napier.d(tag = TAG) { "onRemoveStream: $p0" }
        }

        override fun onDataChannel(p0: DataChannel?) {
            Napier.d(tag = TAG) { "onDataChannel: $p0" }
        }

        override fun onRenegotiationNeeded() {
            Napier.d(tag = TAG) { "onRenegotiationNeeded" }
        }
    }
}

private const val TAG = "WebRTCClient"
private val JSON_INSTANCE = Json { ignoreUnknownKeys = true }