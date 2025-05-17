package org.example.openaitts.feature.realtimeAgent.data

import ai.pipecat.client.helper.LLMFunctionCallResult
import ai.pipecat.client.openai_realtime_webrtc.OpenAIRealtimeWebRTCTransport.AudioDevices
import ai.pipecat.client.result.Future
import ai.pipecat.client.result.RTVIError
import ai.pipecat.client.result.resolvedPromiseErr
import ai.pipecat.client.result.resolvedPromiseOk
import ai.pipecat.client.result.withPromise
import ai.pipecat.client.transport.AuthBundle
import ai.pipecat.client.transport.MsgClientToServer
import ai.pipecat.client.transport.MsgServerToClient
import ai.pipecat.client.transport.Transport
import ai.pipecat.client.transport.TransportContext
import ai.pipecat.client.types.MediaDeviceId
import ai.pipecat.client.types.MediaDeviceInfo
import ai.pipecat.client.types.Participant
import ai.pipecat.client.types.ParticipantId
import ai.pipecat.client.types.ParticipantTracks
import ai.pipecat.client.types.Tracks
import ai.pipecat.client.types.TransportState
import ai.pipecat.client.types.Value
import ai.pipecat.client.types.getOptionsFor
import ai.pipecat.client.types.getValueFor
import android.content.Context
import android.media.AudioManager
import io.github.aakira.napier.Napier
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.example.openaitts.feature.realtimeAgent.domain.OpenAiEvent

class RtcTransport(
    private val transportContext: TransportContext,
    androidContext: Context,
): Transport() {
    private val appContext = androidContext.applicationContext
    private val thread = transportContext.thread
    private var client: WebRTCClient? = null
    private var state = TransportState.Disconnected

    private val eventHandler = { msg: OpenAiEvent ->
        //TODO:
    }

    override fun initDevices(): Future<Unit, RTVIError> {
        return resolvedPromiseOk(thread, Unit)
    }

    override fun connect(authBundle: AuthBundle?): Future<Unit, RTVIError> {
        val options = transportContext.options.params.config.getOptionsFor(SERVICE_LLM)
        val apiKey = (options?.getValueFor(OPTION_API_KEY) as? Value.Str)?.value
        val model = (options?.getValueFor(OPTION_MODEL) as? Value.Str)?.value
        if (apiKey == null || model == null) {
            return resolvedPromiseErr(thread, RTVIError.OtherError("API: $apiKey, model: $model"))
        }
        if (client != null) {
            return resolvedPromiseErr(thread, RTVIError.OtherError("Already connected"))
        }

        transportContext.callbacks.onInputsUpdated(
            camera = false,
            mic = transportContext.options.enableMic
        )

        setState(TransportState.Connecting)

        return thread.runOnThreadReturningFuture {
            Napier.d(tag = TAG) { "Connecting with $$authBundle" }

            try {
                client = WebRTCClient(eventHandler, appContext)
            } catch (e: Exception) {
                return@runOnThreadReturningFuture resolvedPromiseErr(
                    thread,
                    RTVIError.ExceptionThrown(e)
                )
            }

            enableMic(transportContext.options.enableMic)

            withPromise(thread) { promise ->
                MainScope().launch {
                    try {
                        client?.negotiateConnection(
                            baseUrl = "https://api.openai.com/v1/realtime",
                            apiKey = apiKey,
                            model = model
                        )
                        val cb = transportContext.callbacks
                        setState(TransportState.Connected)

                        cb.apply {
                            onConnected()
                            onParticipantJoined(LOCAL_PARTICIPANT)
                            onParticipantJoined(BOT_PARTICIPANT)
                            onBotReady("local", emptyList())
                        }

                        setState(TransportState.Ready)
                        promise.resolveOk(Unit)
                    } catch (e: Exception) {
                        promise.resolveErr(RTVIError.ExceptionThrown(e))
                    }
                }
            }
        }
    }

    override fun disconnect(): Future<Unit, RTVIError> {
        return thread.runOnThreadReturningFuture {
            withPromise(thread) { promise ->
                val clientCopy = client
                client = null

                MainScope().launch {
                    try {
                        if (clientCopy != null) {
                            clientCopy.dispose()
                            setState(TransportState.Disconnected)
                            transportContext.callbacks.onDisconnected()
                        }
                        promise.resolveOk(Unit)
                    } catch (e: Exception) {
                        promise.resolveErr(RTVIError.ExceptionThrown(e))
                    }
                }
            }
        }
    }

    override fun enableMic(enable: Boolean): Future<Unit, RTVIError> {
        thread.runOnThread {
            client?.setAudioTrackEnabled(enable)
            transportContext.callbacks.onInputsUpdated(camera = false, mic = enable)
        }
        return resolvedPromiseOk(thread, Unit)
    }

    override fun getAllMics(): Future<List<MediaDeviceInfo>, RTVIError> {
        return resolvedPromiseOk(thread, listOf(AudioDevices.Earpiece, AudioDevices.Speakerphone))
    }

    override fun isMicEnabled(): Boolean {
        return client?.isAudioTrackEnabled() == true
    }

    override fun release() {
        disconnect().logError(tag = TAG, description = "Disconnect triggered with release")
    }

    override fun selectedMic(): MediaDeviceInfo {
        val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        return when (audioManager.isSpeakerphoneOn) {
            true -> AudioDevices.Speakerphone
            false -> AudioDevices.Earpiece
        }
    }

    override fun sendMessage(message: MsgClientToServer): Future<Unit, RTVIError> {
        return when (message.type) {
            "action" -> {
                try {
                    val data = JSON.decodeFromJsonElement<MsgClientToServer.Data.Action>(message.data!!)

                    when (data.action) {
                        "append_to_messages" -> {
                            val messages: List<Value.Object> =
                                (data.arguments.getValueFor("messages") as Value.Array).value.map { it as Value.Object }

                            for (appendedMessage in messages) {
                                val role = appendedMessage.value["role"] as Value.Str
                                val content = appendedMessage.value["content"] as Value.Str

                                Napier.d(tag = TAG) { "${role.value} sending message: '${content.value}'" }
                                sendConversationMessage(role.value, content.value)
                            }

                            requestResponseFromBot()

                            transportContext.onMessage(
                                MsgServerToClient(
                                    id = message.id,
                                    label = message.label,
                                    type = MsgServerToClient.Type.ActionResponse,
                                    data = JSON.encodeToJsonElement(
                                        MsgServerToClient.Data.ActionResponse(
                                            Value.Null
                                        )
                                    )
                                )
                            )

                            resolvedPromiseOk(thread, Unit)
                        }
                        else -> notSupported()
                    }
                } catch (e: Exception) {
                    resolvedPromiseErr(thread, RTVIError.ExceptionThrown(e))
                }
            }
            "llm-function-call-result" -> {
                val messageData = message.data ?: return resolvedPromiseErr(
                    thread,
                    RTVIError.OtherError("Message data is null")
                )

                val data: LLMFunctionCallResult = JSON.decodeFromJsonElement(messageData)

                client?.sendDataMessage(
                    Value.serializer(),
                    Value.Object(
                        "type" to Value.Str("conversation.item.create"),
                        "item" to Value.Object(
                            "type" to Value.Str("function_call_output"),
                            "call_id" to Value.Str(data.toolCallId),
                            "output" to Value.Str(JSON.encodeToString(data.result))
                        )
                    )
                )
                requestResponseFromBot()

                resolvedPromiseOk(thread, Unit)
            }
            else -> notSupported()
        }
    }

    override fun setState(state: TransportState) {
        Napier.d(tag = TAG) { "State: setting $state" }
        thread.assertCurrent()
        this.state = state
        transportContext.callbacks.onTransportStateChanged(state)
    }

    override fun state(): TransportState = state

    override fun tracks(): Tracks {
        return Tracks(local = ParticipantTracks(null, null), bot = null)
    }

    override fun updateMic(micId: MediaDeviceId): Future<Unit, RTVIError> {
        val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setSpeakerphoneOn(micId == AudioDevices.Speakerphone.id)
        return resolvedPromiseOk(thread, Unit)
    }

    fun requestResponseFromBot() {
        client?.sendDataMessage(
            OpenAIResponseCreate.serializer(),
            OpenAIResponseCreate.new()
        )
    }

    fun sendConversationMessage(role: String, text: String) {
        client?.sendDataMessage(
            OpenAIConversationItemCreate.serializer(),
            OpenAIConversationItemCreate.of(
                OpenAIConversationItemCreate.Item.message(
                    role = role,
                    text = text
                )
            )
        )
    }

    //region Not needed/supported
    private fun <E> notSupported(): Future<E, RTVIError> {
        return resolvedPromiseErr(thread, RTVIError.OtherError("not needed"))
    }
    override fun expiry(): Long? = null
    override fun isCamEnabled(): Boolean = false
    override fun enableCam(enable: Boolean): Future<Unit, RTVIError> = notSupported()
    override fun getAllCams(): Future<List<MediaDeviceInfo>, RTVIError> = notSupported()
    override fun selectedCam(): MediaDeviceInfo? = null
    override fun updateCam(camId: MediaDeviceId): Future<Unit, RTVIError> = notSupported()
    //endregion

    companion object {
        private const val TAG = "RtcTransport"
        private const val SERVICE_LLM = "llm"
        private const val OPTION_API_KEY = "api_key"
        private const val OPTION_INITIAL_MESSAGES = "initial_messages"
        private const val OPTION_INITIAL_CONFIG = "initial_config"
        private const val OPTION_MODEL = "model"

        private val BOT_PARTICIPANT = Participant(
            id = ParticipantId("bot"),
            name = null,
            local = false
        )

        private val LOCAL_PARTICIPANT = Participant(
            id = ParticipantId("local"),
            name = null,
            local = true
        )
    }
}

private val JSON = Json { ignoreUnknownKeys = true }