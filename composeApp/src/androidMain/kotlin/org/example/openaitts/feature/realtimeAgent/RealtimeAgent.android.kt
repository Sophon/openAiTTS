package org.example.openaitts.feature.realtimeAgent

import ai.pipecat.client.RTVIClient
import ai.pipecat.client.RTVIClientOptions
import ai.pipecat.client.RTVIClientParams
import ai.pipecat.client.RTVIEventCallbacks
import ai.pipecat.client.helper.LLMContextMessage
import ai.pipecat.client.openai_realtime_webrtc.OpenAIRealtimeSessionConfig
import ai.pipecat.client.openai_realtime_webrtc.OpenAIRealtimeWebRTCTransport
import ai.pipecat.client.result.Future
import ai.pipecat.client.result.RTVIError
import ai.pipecat.client.transport.MsgServerToClient
import ai.pipecat.client.types.ServiceConfig
import ai.pipecat.client.types.Tracks
import ai.pipecat.client.types.Transcript
import ai.pipecat.client.types.TransportState
import ai.pipecat.client.types.Value
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import io.github.aakira.napier.Napier
import org.example.openaitts.core.PlatformContext
import org.example.openaitts.feature.conversation.domain.models.Voice

actual class RealtimeAgent actual constructor(
    callbacks: RealtimeAgentCallbacks,
) {
    private val client = mutableStateOf<RTVIClient?>(null)
    val state = mutableStateOf(TransportState.Disconnected)
    val errors = mutableStateListOf<Error>()
    val tracks = mutableStateOf<Tracks?>(null) //TODO: investigate

    actual val platformContext = PlatformContext

    private val eventCallbacks = object : RTVIEventCallbacks() {
        override fun onBackendError(message: String) = callbacks.onBackendError(message)

        override fun onConnected() = callbacks.onConnect()

        override fun onDisconnected() {
            callbacks.onDisconnect()

            client.value?.release()
            client.value = null
            state.value = TransportState.Disconnected
            tracks.value = null
        }

        override fun onBotReady(version: String, config: List<ServiceConfig>) {
            callbacks.onAgentReady()
//            client.value?.action() //TODO: investigate how we can add transcription
        }

        override fun onBotTTSText(data: MsgServerToClient.Data.BotTTSTextData) {
            callbacks.onAgentTranscriptionReceived(transcript = data.text)
        }

        override fun onBotTranscript(text: String) {
            super.onBotTranscript(text)

            Napier.d(tag = TAG) { "Bot transcript: $text" }
        }

        override fun onBotLLMText(data: MsgServerToClient.Data.BotLLMTextData) {
            super.onBotLLMText(data)

            Napier.d(tag = TAG) { "Bot LLM text: ${data.text}" }
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


        override fun onTransportStateChanged(state: TransportState) {
            Napier.d(tag = TAG) { "transport state changed: $state" }
            this@RealtimeAgent.state.value = state
        }

        override fun onTracksUpdated(tracks: Tracks) {
            this@RealtimeAgent.tracks.value = tracks
        }
    }

    actual fun start(apiKey: String, voice: Voice) {
        if (client.value != null) return

        val client = RTVIClient(
            transport = OpenAIRealtimeWebRTCTransport.Factory(platformContext.get()),
            options = createOptions(apiKey, voice.name.lowercase()),
            callbacks = eventCallbacks,
        )

        client
            .connect()
            .displayErrors()
            .withErrorCallback {
                eventCallbacks.onDisconnected()
            }

        this.client.value = client
    }

    actual fun stop() {
        client.value?.disconnect()?.displayErrors()
    }

    actual fun toggleMic(newValue: Boolean) {
        enableMic(newValue)
    }

    private fun enableMic(isEnabled: Boolean) {
        client.value?.enableMic(isEnabled)?.displayErrors()
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

    private fun <E> Future<E, RTVIError>.displayErrors() = withErrorCallback {
        Napier.e(tag = TAG) { "Future resolved with error: ${it.description}; ${it.description}" }
        errors.add(Error(it.description))
    }


    companion object {
        private const val TAG = "RealtimeAgent"
    }
}

@Immutable
data class Error(val message: String)