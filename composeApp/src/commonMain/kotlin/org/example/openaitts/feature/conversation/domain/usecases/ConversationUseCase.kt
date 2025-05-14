package org.example.openaitts.feature.conversation.domain.usecases

import com.shepeliev.webrtckmp.AudioStreamTrack
import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.MediaDevices
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.MediaStreamTrack
import com.shepeliev.webrtckmp.MediaStreamTrackKind
import com.shepeliev.webrtckmp.OfferAnswerOptions
import com.shepeliev.webrtckmp.PeerConnection
import com.shepeliev.webrtckmp.RtcConfiguration
import com.shepeliev.webrtckmp.SessionDescription
import com.shepeliev.webrtckmp.SessionDescriptionType
import com.shepeliev.webrtckmp.SignalingState
import com.shepeliev.webrtckmp.TrackEvent
import com.shepeliev.webrtckmp.onConnectionStateChange
import com.shepeliev.webrtckmp.onIceCandidate
import com.shepeliev.webrtckmp.onIceConnectionStateChange
import com.shepeliev.webrtckmp.onSignalingStateChange
import com.shepeliev.webrtckmp.onTrack
import io.github.aakira.napier.Napier
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.json.Json
import org.example.openaitts.core.data.MODEL_REALTIME
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.core.domain.map
import org.example.openaitts.core.domain.onError
import org.example.openaitts.core.domain.onSuccess
import org.example.openaitts.feature.audio.AudioPlayer
import org.example.openaitts.feature.conversation.data.RealtimeWebRtcDataSource
import org.example.openaitts.feature.conversation.data.RealtimeWebSocketDataSource
import org.example.openaitts.feature.conversation.data.dto.RequestSessionDto
import org.example.openaitts.feature.conversation.data.dto.RequestUpdateSessionDto
import org.example.openaitts.feature.conversation.data.dto.ResponseDto
import org.example.openaitts.feature.conversation.domain.models.Content
import org.example.openaitts.feature.conversation.domain.models.EventType
import org.example.openaitts.feature.conversation.domain.models.MessageItem
import org.example.openaitts.feature.conversation.domain.models.Role
import org.example.openaitts.feature.conversation.domain.models.Session
import org.example.openaitts.feature.conversation.domain.utils.decode

class ConversationUseCase(
    private val remoteDataSource: RealtimeWebSocketDataSource,
    private val rtcSource: RealtimeWebRtcDataSource,
    private val audioPlayer: AudioPlayer,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var playbackStarted = false

    private var session: Session? = null
    private val pc1 = PeerConnection()
    private val pc2 = PeerConnection()
    private lateinit var localStream: MediaStream

    suspend fun establishConnection(): Flow<Result<MessageItem, DataError.Remote>> {
        remoteDataSource.closeWebsocketSession()

        val flow = remoteDataSource.initializeWebSocketSession(
            processText = ::processText,
            processBinary = ::processBinary,
        ).map { dto ->
            when {
                (dto.delta != null) -> {
                    Napier.d(tag = TAG) { "delta" }
                    Result.Success(createMessageItemFromDelta(dto))
                }
                (dto.item == null) -> {
                    Result.Error(DataError.Remote.UNKNOWN)
                }
                else -> {
                    Result.Success(dto.item)
                }
            }
        }

        updateToAudioTranscription()

        return flow
    }

    suspend fun establishRTCConnection(scope: CoroutineScope) {
        getSession()

        configurePeerConnections(scope) { audioStreamTrack ->
            //TODO: implement what to do with the received track
            Napier.d(tag = TAG2) { "audio stream track id: ${audioStreamTrack.id}" }
        }

        signal()
    }

    private suspend fun getSession() {
        rtcSource.getSession(RequestSessionDto(model = MODEL_REALTIME))
            .onSuccess { session ->
                Napier.d(tag = TAG2) { "Succcess; ${session.clientSecret}" }
                this.session = session
            }
            .onError { error ->
                Napier.d(tag = TAG2) { error.toString() }
            }
    }

    private suspend fun configurePeerConnections(
        scope: CoroutineScope,
        onRemoteAudioTrack: (AudioStreamTrack) -> Unit,
    ) {
        val pc1IceCandidates = mutableListOf<IceCandidate>()
        val pc2IceCandidates = mutableListOf<IceCandidate>()

        pc1.apply {
            localStream = MediaDevices.getUserMedia(video = false, audio = true)
            localStream.tracks.forEach { pc1.addTrack(it) }

            onIceCandidate.onEach { iceCandidate ->
                Napier.d(tag = TAG2) { "onIceCandidate (PC1): $iceCandidate" }
                if (pc2.signalingState == SignalingState.HaveRemoteOffer) {
                    pc2.addIceCandidate(iceCandidate)
                } else {
                    pc1IceCandidates.add(iceCandidate)
                }
            }.launchIn(scope)

            onSignalingStateChange.onEach { signalingState ->
                Napier.d(tag = TAG2) { "onSignalingStateChange (PC1): $signalingState" }
                if (signalingState == SignalingState.HaveRemoteOffer) {
                    pc2IceCandidates.forEach { pc1.addIceCandidate(it) }
                    pc2IceCandidates.clear()
                }
            }.launchIn(scope)

            onIceConnectionStateChange.onEach {
                Napier.d(tag = TAG2) { "onIceConnectionStateChange (PC1): $it" }
            }.launchIn(scope)

            onConnectionStateChange.onEach { peerConnectionState ->
                Napier.d(tag = TAG2) { "onConnectionStateChange (PC1): $peerConnectionState" }
            }.launchIn(scope)

            onTrack.onEach { trackEvent ->
                Napier.d(tag = TAG2) { "onTrack (PC1): $trackEvent" }
            }.launchIn(scope)
        }

        pc2.apply {
            onIceCandidate.onEach { iceCandidate ->
                Napier.d(tag = TAG2) { "onIceCandidate (PC2): $iceCandidate" }
                if (pc1.signalingState == SignalingState.HaveRemoteOffer) {
                    pc1.addIceCandidate(iceCandidate)
                } else {
                    pc2IceCandidates.add(iceCandidate)
                }
            }.launchIn(scope)

            onSignalingStateChange.onEach { signalingState ->
                Napier.d(tag = TAG2) { "onSignalingStateChange (PC2): $signalingState" }
                if (signalingState == SignalingState.HaveRemoteOffer) {
                    pc1IceCandidates.forEach { pc2.addIceCandidate(it) }
                    pc1IceCandidates.clear()
                }
            }.launchIn(scope)

            onIceConnectionStateChange.onEach {
                Napier.d(tag = TAG2) { "onIceConnectionStateChange (PC2): $it" }
            }.launchIn(scope)

            onConnectionStateChange.onEach { peerConnectionState ->
                Napier.d(tag = TAG2) { "onConnectionStateChange (PC2): $peerConnectionState" }
            }.launchIn(scope)

            onTrack
                .onEach { trackEvent ->
                    Napier.d(tag = TAG2) { "PC2 onTrack: ${trackEvent.track?.kind}" }
                }
                .map { it.track }
                .filterNotNull()
                .onEach {
                    if (it.kind == MediaStreamTrackKind.Audio) {
                        onRemoteAudioTrack(it as AudioStreamTrack)
                    }
                }
                .launchIn(scope)
        }
    }

    private suspend fun signal() {
        val offer = pc1.createOffer(OfferAnswerOptions(offerToReceiveVideo = false, offerToReceiveAudio = true))
            .also { offer ->
                pc1.setLocalDescription(offer)
                pc2.setRemoteDescription(offer)
            }

        session?.clientSecret?.value?.let { ephemeralKey ->
            rtcSource.postSdp(ephemeralKey = ephemeralKey, sdp = offer.sdp)
                .onSuccess { answer ->
                    val description = SessionDescription(
                        type = SessionDescriptionType.Answer,
                        sdp = answer,
                    )
                    pc2.setLocalDescription(description)
                    pc1.setRemoteDescription(description)
                }
                .onError { error -> Napier.e(tag = TAG2) { error.toString() } }
        }
    }

    //returning null means we're not handling it
    private fun processText(textFrame: Frame.Text): ResponseDto? {
        val text = textFrame.readText()
        val eventObject = json.decodeFromString<ResponseDto>(text)

        return when (eventObject.type) {
            EventType.RESPONSE_OUTPUT_ITEM_DONE -> {
                Napier.d(tag = TAG) { "response output item done: ${eventObject.item?.content?.firstOrNull()?.text}" }
                eventObject
            }
            EventType.RESPONSE_AUDIO_DELTA -> {
                Napier.d(tag = TAG) { "received audio chunk" }
                eventObject.delta?.let { rawData ->
                    audioPlayer.apply {
                        cache(rawData.decode())
                        if (playbackStarted.not()) {
                            play()
                            playbackStarted = true
                        }
                    }
                }
                null
            }
            EventType.ITEM_CREATED -> {
                Napier.d(tag = TAG) { "item created: $eventObject" }
                null
            }
            EventType.RESPONSE_AUDIO_DONE -> {
                Napier.d(tag = TAG) { "audio chunks done" }
                eventObject
            }
            EventType.RESPONSE_AUDIO_TRANSCRIPT_DELTA -> {
                Napier.d(tag = TAG) { "received a chunk of the audio transcript" }
                eventObject
            }
            EventType.RESPONSE_AUDIO_TRANSCRIPT_DONE -> {
                Napier.d(tag = TAG) { "audio transcript done" }
                null
            }
            EventType.SESSION_UPDATED -> {
                Napier.d(tag = TAG) { "session updated: $eventObject" }
                null
            }
            EventType.ERROR -> {
                Napier.e(tag = TAG) { "error: $eventObject" }
                null
            }
            else -> {
                Napier.d(tag = TAG) { "unhandled event: $eventObject" }
                null
            }
        }
    }

    private fun processBinary(binaryFrame: Frame.Binary): ResponseDto? {
        Napier.d(tag = TAG) { "received binary frame; not handled" }

        return null
    }

    private fun createMessageItemFromDelta(dto: ResponseDto): MessageItem {
        return MessageItem(
            type = MessageItem.Type.MESSAGE,
            role = Role.ASSISTANT,
            content = listOf(
                Content(
                    type = Content.Type.TEXT,
                    text = dto.delta,
                )
            ),
            isIncomplete = true,
        )
    }

    private suspend fun updateToAudioTranscription() {
        val dto = RequestUpdateSessionDto(
            type = EventType.SESSION_UPDATE,
            session = Session(
                inputAudioTranscription = Session.InputAudioTranscription(
                    language = "en",
                    model = "whisper-1",
                ),
            )
        )

        remoteDataSource.updateSession(dto)
    }
}

private const val TAG = "ConversationUseCase"
private const val TAG2 = "WebRTCKMP"