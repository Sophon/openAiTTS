//package org.example.openaitts.feature.conversation.data
//
//import com.shepeliev.webrtckmp.AudioStreamTrack
//import com.shepeliev.webrtckmp.IceCandidate
//import com.shepeliev.webrtckmp.MediaDevices
//import com.shepeliev.webrtckmp.MediaStream
//import com.shepeliev.webrtckmp.MediaStreamTrackKind
//import com.shepeliev.webrtckmp.OfferAnswerOptions
//import com.shepeliev.webrtckmp.PeerConnection
//import com.shepeliev.webrtckmp.SessionDescription
//import com.shepeliev.webrtckmp.SessionDescriptionType
//import com.shepeliev.webrtckmp.SignalingState
//import com.shepeliev.webrtckmp.onConnectionStateChange
//import com.shepeliev.webrtckmp.onIceCandidate
//import com.shepeliev.webrtckmp.onIceConnectionStateChange
//import com.shepeliev.webrtckmp.onSignalingStateChange
//import com.shepeliev.webrtckmp.onTrack
//import io.github.aakira.napier.Napier
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.flow.filterNotNull
//import kotlinx.coroutines.flow.launchIn
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.flow.onEach
//import org.example.openaitts.core.data.MODEL_REALTIME
//import org.example.openaitts.core.domain.onError
//import org.example.openaitts.core.domain.onSuccess
//import org.example.openaitts.feature.conversation.data.dto.RequestSessionDto
//import org.example.openaitts.feature.conversation.domain.models.Session
//
//class RtcConnectionManager(
//    private val remoteDataSource: RealtimeWebRtcDataSource,
//) {
//    private var session: Session? = null
//    private val pc1 = PeerConnection()
//    private val pc2 = PeerConnection()
//    private lateinit var localStream: MediaStream
//    private val pc1IceCandidates = mutableListOf<IceCandidate>()
//    private val pc2IceCandidates = mutableListOf<IceCandidate>()
//
//    suspend fun establishRTCConnection(scope: CoroutineScope) {
//        getSession()
//
//        configurePeerConnections(scope) { audioStreamTrack ->
//            //TODO: implement what to do with the received track
//            Napier.d(tag = TAG) { "audio stream track id: ${audioStreamTrack.id}" }
//        }
//
//        signal()
//    }
//
//    fun closeConnection() {
//        pc1.close()
//        pc2.close()
//        pc1IceCandidates.clear()
//        pc2IceCandidates.clear()
//        localStream.release()
//    }
//
//    private suspend fun getSession() {
//        remoteDataSource.getSession(RequestSessionDto(model = MODEL_REALTIME))
//            .onSuccess { session ->
//                Napier.d(tag = TAG) { "Succcess; ${session.clientSecret}" }
//                this.session = session
//            }
//            .onError { error ->
//                Napier.d(tag = TAG) { error.toString() }
//            }
//    }
//
//    private suspend fun configurePeerConnections(
//        scope: CoroutineScope,
//        onRemoteAudioTrack: (AudioStreamTrack) -> Unit,
//    ) {
//        pc1.apply {
//            localStream = MediaDevices.getUserMedia(video = false, audio = true)
//            localStream.tracks.forEach { pc1.addTrack(it) }
//
//            onIceCandidate.onEach { iceCandidate ->
//                Napier.d(tag = TAG) { "onIceCandidate (PC1): $iceCandidate" }
//                if (pc2.signalingState == SignalingState.HaveRemoteOffer) {
//                    pc2.addIceCandidate(iceCandidate)
//                } else {
//                    pc1IceCandidates.add(iceCandidate)
//                }
//            }.launchIn(scope)
//
//            onSignalingStateChange.onEach { signalingState ->
//                Napier.d(tag = TAG) { "onSignalingStateChange (PC1): $signalingState" }
//                if (signalingState == SignalingState.HaveRemoteOffer) {
//                    pc2IceCandidates.forEach { pc1.addIceCandidate(it) }
//                    pc2IceCandidates.clear()
//                }
//            }.launchIn(scope)
//
//            onIceConnectionStateChange.onEach {
//                Napier.d(tag = TAG) { "onIceConnectionStateChange (PC1): $it" }
//            }.launchIn(scope)
//
//            onConnectionStateChange.onEach { peerConnectionState ->
//                Napier.d(tag = TAG) { "onConnectionStateChange (PC1): $peerConnectionState" }
//            }.launchIn(scope)
//
//            onTrack.onEach { trackEvent ->
//                Napier.d(tag = TAG) { "onTrack (PC1): $trackEvent" }
//            }.launchIn(scope)
//        }
//
//        pc2.apply {
//            onIceCandidate.onEach { iceCandidate ->
//                Napier.d(tag = TAG) { "onIceCandidate (PC2): $iceCandidate" }
//                if (pc1.signalingState == SignalingState.HaveRemoteOffer) {
//                    pc1.addIceCandidate(iceCandidate)
//                } else {
//                    pc2IceCandidates.add(iceCandidate)
//                }
//            }.launchIn(scope)
//
//            onSignalingStateChange.onEach { signalingState ->
//                Napier.d(tag = TAG) { "onSignalingStateChange (PC2): $signalingState" }
//                if (signalingState == SignalingState.HaveRemoteOffer) {
//                    pc1IceCandidates.forEach { pc2.addIceCandidate(it) }
//                    pc1IceCandidates.clear()
//                }
//            }.launchIn(scope)
//
//            onIceConnectionStateChange.onEach {
//                Napier.d(tag = TAG) { "onIceConnectionStateChange (PC2): $it" }
//            }.launchIn(scope)
//
//            onConnectionStateChange.onEach { peerConnectionState ->
//                Napier.d(tag = TAG) { "onConnectionStateChange (PC2): $peerConnectionState" }
//            }.launchIn(scope)
//
//            onTrack
//                .onEach { trackEvent ->
//                    Napier.d(tag = TAG) { "PC2 onTrack: ${trackEvent.track?.kind}" }
//                }
//                .map { it.track }
//                .filterNotNull()
//                .onEach {
//                    if (it.kind == MediaStreamTrackKind.Audio) {
//                        onRemoteAudioTrack(it as AudioStreamTrack)
//                    }
//                }
//                .launchIn(scope)
//        }
//    }
//
//    private suspend fun signal() {
//        val offer = pc1.createOffer(OfferAnswerOptions(offerToReceiveVideo = false, offerToReceiveAudio = true))
//            .also { offer ->
//                pc1.setLocalDescription(offer)
//                pc2.setRemoteDescription(offer)
//            }
//
//        session?.clientSecret?.value?.let { ephemeralKey ->
//            remoteDataSource.postSdp(ephemeralKey = ephemeralKey, sdp = offer.sdp)
//                .onSuccess { answer ->
//                    val description = SessionDescription(
//                        type = SessionDescriptionType.Answer,
//                        sdp = answer,
//                    )
//                    pc2.setLocalDescription(description)
//                    pc1.setRemoteDescription(description)
//                }
//                .onError { error -> Napier.e(tag = TAG) { error.toString() } }
//        }
//    }
//}
//
//private const val TAG = "RtcConnectionManager"