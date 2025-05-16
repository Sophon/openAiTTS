package org.example.openaitts.feature.realtimeAgent.data

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
import org.example.openaitts.core.PlatformContext
import org.example.openaitts.feature.conversation.domain.models.Voice
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

actual class WebRTCClient actual constructor(
    onIncomingEvent: (OpenAiEvent) -> Unit,
    platformContext: PlatformContext,
) {
    private val peerConnectionFactory: PeerConnectionFactory
    private val peerConnection: PeerConnection
    private val negotiateJob = AtomicReference<Job?>(null)
    private val audioSource: AudioSource
    private val localAudioTrack: AudioTrack
    private val dataChannel: DataChannel

    init {
        val options = PeerConnectionFactory.Options()
        val audioDeviceModule = JavaAudioDeviceModule.builder(platformContext.get())
            .createAudioDeviceModule()

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setAudioDeviceModule(audioDeviceModule)
            .createPeerConnectionFactory()

        Napier.d(tag = TAG) { "Peer connection factory initialized" }

        val iceServers = ArrayList<PeerConnection.IceServer>()
        iceServers.add(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN

        peerConnection =
            peerConnectionFactory.createPeerConnection(rtcConfig, LoggingPeerConnectionObserver)
                ?: throw IllegalStateException("Failed to create PeerConnection")

        Napier.d(tag = TAG) { "Peer connection created" }

        audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory.createAudioTrack("mic", audioSource)
        localAudioTrack.setEnabled(true)

        val sender = peerConnection.addTrack(localAudioTrack)
        Napier.d(tag = TAG) { if (sender != null) "Adding audio track pc: success" else "Adding audio track pc: failed" }

        dataChannel = peerConnection.createDataChannel("oai-events", DataChannel.Init())
        dataChannel.registerObserver(
            object : DataChannel.Observer {
                override fun onBufferedAmountChange(p0: Long) {
                    TODO("Not yet implemented")
                }

                override fun onStateChange() {
                    TODO("Not yet implemented")
                }

                override fun onMessage(p0: DataChannel.Buffer?) {
                    TODO("Not yet implemented")
                }
            }
        )
    }

    actual fun start(apiKey: String, voice: Voice) {}

    actual fun stop() {}

    actual fun toggleMic(newValue: Boolean) {}

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

    private suspend fun dispose() {
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

    private fun setAudioTrackEnabled(enabled: Boolean) {
        localAudioTrack.setEnabled(enabled)
    }

    private fun isAudioTrackEnabled(): Boolean = localAudioTrack.enabled()

    private fun <T> sendDataMessage(serializer: KSerializer<T>, msg: T) {
        val msgString = JSON_INSTANCE.encodeToString(serializer, msg)

        dataChannel.send(
            DataChannel.Buffer(
                ByteBuffer.wrap(msgString.toByteArray(charset = Charsets.UTF_8)),
                false
            )
        )
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

    private suspend fun negotiateConnection(
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


    private object LoggingPeerConnectionObserver: PeerConnection.Observer {
        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
            TODO("Not yet implemented")
        }

        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
            TODO("Not yet implemented")
        }

        override fun onIceConnectionReceivingChange(p0: Boolean) {
            TODO("Not yet implemented")
        }

        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
            TODO("Not yet implemented")
        }

        override fun onIceCandidate(p0: IceCandidate?) {
            TODO("Not yet implemented")
        }

        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
            TODO("Not yet implemented")
        }

        override fun onAddStream(p0: MediaStream?) {
            TODO("Not yet implemented")
        }

        override fun onRemoveStream(p0: MediaStream?) {
            TODO("Not yet implemented")
        }

        override fun onDataChannel(p0: DataChannel?) {
            TODO("Not yet implemented")
        }

        override fun onRenegotiationNeeded() {
            TODO("Not yet implemented")
        }
    }
}

private const val TAG = "WebRTCClient"
private val JSON_INSTANCE = Json { ignoreUnknownKeys = true }