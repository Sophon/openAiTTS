package org.example.openaitts.feature.realtimeAgent

import ai.pipecat.client.RTVIClientOptions
import ai.pipecat.client.RTVIClientParams
import ai.pipecat.client.RTVIEventCallbacks
import ai.pipecat.client.helper.LLMContextMessage
import ai.pipecat.client.openai_realtime_webrtc.OpenAIRealtimeSessionConfig
import ai.pipecat.client.openai_realtime_webrtc.OpenAIRealtimeWebRTCTransport
import ai.pipecat.client.transport.AuthBundle
import ai.pipecat.client.transport.MsgServerToClient
import ai.pipecat.client.transport.TransportContext
import ai.pipecat.client.types.ServiceConfig
import ai.pipecat.client.types.Tracks
import ai.pipecat.client.types.Transcript
import ai.pipecat.client.types.Value
import ai.pipecat.client.utils.ThreadRef
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import io.github.aakira.napier.Napier
import org.example.openaitts.core.PlatformContext
import org.example.openaitts.feature.conversation.domain.models.PROMPT
import org.example.openaitts.feature.conversation.domain.models.Voice
import org.example.openaitts.feature.realtimeAgent.data.RtcTransport

actual class RealtimeAgent actual constructor(
    callbacks: RealtimeAgentCallbacks,
) {
    val errors = mutableStateListOf<Error>()
    val tracks = mutableStateOf<Tracks?>(null)
    actual val platformContext = PlatformContext

    private val eventCallbacks = object : RTVIEventCallbacks() {
        override fun onBackendError(message: String) {
            errors.add(Error(message))
            callbacks.onBackendError(message)
        }

        override fun onConnected() = callbacks.onConnect()

        override fun onDisconnected() {
            callbacks.onDisconnect()
            tracks.value = null
        }

        override fun onBotReady(version: String, config: List<ServiceConfig>) {
            callbacks.onAgentReady()
        }

        //bot transcription shit
        override fun onBotTTSText(data: MsgServerToClient.Data.BotTTSTextData) {
            Napier.d(tag = TAG) { "onBotTTSText delta: ${data.text}" }
            callbacks.onAgentTranscriptionDeltaReceived(transcript = data.text)
        }

        override fun onBotTTSStopped() {
            Napier.d(tag = TAG) { "onBotTTSStopped" }
            callbacks.onAgentTranscriptionDone()
        }

        override fun onUserTranscript(data: Transcript) {
            callbacks.onUserTranscriptionReceived(text = data.text, isFinal = data.final)
        }

        override fun onBotStartedSpeaking() = callbacks.onAgentTalking()

        override fun onBotStoppedSpeaking() {
            callbacks.onAgentTalkingDone()
        }

        override fun onUserStartedSpeaking() = callbacks.onUserTalking()

        override fun onUserStoppedSpeaking() = callbacks.onUserTalkingDone()
    }

    private var apiKey: String = ""
    private var voice: Voice = Voice.ECHO
    private val transportContext by lazy {
        object : TransportContext {
            override val options: RTVIClientOptions = createOptions(apiKey, voice.name.lowercase())
            override val callbacks: RTVIEventCallbacks = eventCallbacks
            override val thread = ThreadRef.forCurrent()

            override fun onMessage(msg: MsgServerToClient) {
                Napier.d(tag = TAG) { "onMessage of $msg" }
            }
        }
    }
    private val transport by lazy {
        RtcTransport.Factory(platformContext.get()).createTransport(context = transportContext)
    }

    actual fun start(apiKey: String, voice: Voice) {
        this.apiKey = apiKey
        this.voice = voice
        val authBundle = AuthBundle(data = "")

        transport.connect(authBundle)
    }

    actual fun stop() {
        transport.disconnect()
    }

    actual fun toggleMic(newValue: Boolean) {
        enableMic(newValue)
    }

    private fun enableMic(isEnabled: Boolean) {
        Napier.d(tag = TAG) { "toggling mic: $isEnabled" }
    }

    private fun createOptions(apiKey: String, voice: String): RTVIClientOptions {
        return RTVIClientOptions(
            params = RTVIClientParams(
                baseUrl = null,
                config = OpenAIRealtimeWebRTCTransport.buildConfig(
                    apiKey = apiKey,
                    initialMessages = listOf(
                        LLMContextMessage(
                            role = "user",
                            content = "Tell me your name"
                        ),
                    ),
                    initialConfig = OpenAIRealtimeSessionConfig(
                        instructions = PROMPT,
                        turnDetection = Value.Object(
                            "type" to Value.Str("semantic_vad")
                        ),
                        inputAudioNoiseReduction = Value.Object(
                            "type" to Value.Str("near_field")
                        ),
                        inputAudioTranscription = Value.Object(
                            "model" to Value.Str("whisper-1")
                        ),
                        modalities = listOf("audio", "text"),
                        voice = voice,
                    )
                )
            )
        )
    }


    companion object {
        private const val TAG = "RealtimeAgentNative"
    }
}

@Immutable
data class Error(val message: String)