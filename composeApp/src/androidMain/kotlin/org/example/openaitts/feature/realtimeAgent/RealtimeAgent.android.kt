package org.example.openaitts.feature.realtimeAgent

import ai.pipecat.client.RTVIClient
import ai.pipecat.client.RTVIClientOptions
import ai.pipecat.client.RTVIClientParams
import ai.pipecat.client.RTVIEventCallbacks
import ai.pipecat.client.openai_realtime_webrtc.OpenAIRealtimeSessionConfig
import ai.pipecat.client.openai_realtime_webrtc.OpenAIRealtimeWebRTCTransport
import ai.pipecat.client.result.Future
import ai.pipecat.client.result.RTVIError
import ai.pipecat.client.transport.MsgServerToClient
import ai.pipecat.client.types.ServiceConfig
import ai.pipecat.client.types.Transcript
import ai.pipecat.client.types.TransportState
import ai.pipecat.client.types.Value
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import io.github.aakira.napier.Napier
import org.example.openaitts.core.PlatformContext

actual class RealtimeAgent actual constructor(
    callbacks: RealtimeAgentCallbacks,
    val apiKey: String,
    val platformContext: PlatformContext,
) {
    private val client = mutableStateOf<RTVIClient?>(null)
    val state = mutableStateOf<TransportState>(TransportState.Disconnected)
    val errors = mutableStateListOf<Error>()

    //TODO: these should probably be in the ViewModel; use them in the callbacks
    actual val isBotReady = mutableStateOf(false)
    actual val isBotTalking = mutableStateOf(false)
    actual val isUserTalking = mutableStateOf(false)
    actual val botAudioLevel = mutableFloatStateOf(0f)
    actual val userAudioLevel = mutableFloatStateOf(0f)
    actual val isMicEnabled = mutableStateOf(false)

    private val eventCallbacks = object : RTVIEventCallbacks() {
        override fun onBackendError(message: String) = callbacks.onBackendError(message)

        override fun onConnected() = callbacks.onConnect()

        override fun onDisconnected() = callbacks.onDisconnect()

        override fun onBotReady(version: String, config: List<ServiceConfig>) =
            callbacks.onBotReady()

        override fun onBotTTSText(data: MsgServerToClient.Data.BotTTSTextData) =
            callbacks.onBotTranscriptionReceived(transcript = data.text)

        override fun onUserTranscript(data: Transcript) =
            callbacks.onUserTranscriptionReceived(text = data.text, isFinal = data.final)

        override fun onBotStartedSpeaking() = callbacks.onBotStartedSpeaking()

        override fun onBotStoppedSpeaking() = callbacks.onBotStoppedSpeaking()

        override fun onUserStartedSpeaking() = callbacks.onUserStartedSpeaking()

        override fun onUserStoppedSpeaking() = callbacks.onUserStoppedSpeaking()

        override fun onTransportStateChanged(state: TransportState) {
            this@RealtimeAgent.state.value = state
        }
    }

    actual fun start() {
        if (client.value != null) return

        val client = RTVIClient(
            transport = OpenAIRealtimeWebRTCTransport.Factory(platformContext.get()),
            options = createOptions(),
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

    private fun createOptions(): RTVIClientOptions {
        return RTVIClientOptions(
            params = RTVIClientParams(
                baseUrl = null,
                config = OpenAIRealtimeWebRTCTransport.buildConfig(
                    apiKey = apiKey,
                    /*initialMessages = listOf(
                        LLMContextMessage(
                            role = "user",
                            content = "Please name an interesting landmark."
                        ),
                        LLMContextMessage(
                            role = "assistant",
                            content = "Elizabeth tower."
                        ),
                        LLMContextMessage(
                            role = "user",
                            content = "How tall is it?"
                        )
                    ),*/
                    initialConfig = OpenAIRealtimeSessionConfig(
                        turnDetection = Value.Object(
                            "type" to Value.Str("semantic_vad")
                        ),
                        inputAudioNoiseReduction = Value.Object(
                            "type" to Value.Str("near_field")
                        ),
                        inputAudioTranscription = Value.Object(
                            "model" to Value.Str("whisper-1")
                        )
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