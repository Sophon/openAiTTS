package org.example.openaitts.feature.conversation.data

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.headers
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.openaitts.core.data.MODEL_REALTIME
import org.example.openaitts.core.data.REALTIME_WEBSOCKET_URL
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.EmptyResult
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.conversation.data.dto.RequestCreateItemDto
import org.example.openaitts.feature.conversation.data.dto.RequestResponseDto
import org.example.openaitts.feature.conversation.data.dto.ResponseDto
import org.example.openaitts.feature.conversation.domain.models.EventType

class RealtimeRemoteDataSource(
    private val httpClient: HttpClient,
) {
    private var webSocketSession: WebSocketSession? = null

    suspend fun initializeSession(): Flow<ResponseDto> {
        val url = "$REALTIME_WEBSOCKET_URL?model=$MODEL_REALTIME"
        webSocketSession = httpClient.webSocketSession(url) {
            headers { append("openai-beta", "realtime=v1") }
        }

        val json = Json {
            ignoreUnknownKeys = true
        }
        return flow {
            val messages = webSocketSession!!
                .incoming
                .consumeAsFlow()
                .mapNotNull { frameObject ->
                    when (frameObject) {
                        is Frame.Text -> {
                            val text = frameObject.readText()
                            val eventObject = json.decodeFromString<ResponseDto>(text)

                            when (eventObject.type) {
                                EventType.RESPONSE_OUTPUT_ITEM_DONE -> {
                                    Napier.d(tag = TAG) {
                                        "response output item done: ${eventObject.item?.content?.firstOrNull()?.text}"
                                    }
                                    eventObject
                                }
                                EventType.ERROR -> {
                                    Napier.e(tag = TAG) { "error: ${eventObject.error?.message}" }
                                    null
                                }
                                else -> {
                                    Napier.d(tag = TAG) { "some other event: ${eventObject.type}" }
                                    null
                                }
                            }

                        }
                        is Frame.Binary -> null //TODO: AUDIO
                        else -> null
                    }
                }

            emitAll(messages)
        }
    }

    suspend fun send(message: RequestCreateItemDto): EmptyResult<DataError.Remote> {
        try {
            val json = Json {
                encodeDefaults = false
                ignoreUnknownKeys = true
            }
            val textMessage = json.encodeToString(message)
            webSocketSession?.send(Frame.Text(textMessage))
            return Result.Success(Unit)
        } catch (e: Exception) {
            Napier.e(tag = TAG) { e.message.toString() }
            return Result.Error(DataError.Remote.UNKNOWN)
        }
    }

    suspend fun requestResponse(requestResponseDto: RequestResponseDto): EmptyResult<DataError.Remote> {
        return try {
            val json = Json { encodeDefaults = true }
            val request = json.encodeToString(requestResponseDto)
            webSocketSession?.send(Frame.Text(request))
            Result.Success(Unit)
        } catch (e: Exception) {
            Napier.e(tag = TAG) { e.message.toString() }
            Result.Error(DataError.Remote.UNKNOWN)
        }
    }
}

private const val TAG = "ConversationRemoteDataSource"