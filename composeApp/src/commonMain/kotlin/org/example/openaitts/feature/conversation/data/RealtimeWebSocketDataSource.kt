package org.example.openaitts.feature.conversation.data

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.headers
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
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
import org.example.openaitts.feature.conversation.data.dto.RequestUpdateSessionDto
import org.example.openaitts.feature.conversation.data.dto.ResponseDto

class RealtimeWebSocketDataSource(
    private val httpClient: HttpClient,
) {
    private var webSocketSession: WebSocketSession? = null

    suspend fun initializeWebSocketSession(
        processText: (Frame.Text) -> ResponseDto?,
        processBinary: (Frame.Binary) -> ResponseDto?,
    ): Flow<ResponseDto> {
        val url = "$REALTIME_WEBSOCKET_URL?model=$MODEL_REALTIME"
        webSocketSession = httpClient.webSocketSession(url) {
            headers { append("openai-beta", "realtime=v1") }
        }

        return webSocketSession!!
            .incoming
            .consumeAsFlow()
            .mapNotNull { frameObject ->
                when (frameObject) {
                    is Frame.Text -> processText(frameObject)
                    is Frame.Binary -> processBinary(frameObject)
                    else -> null
                }
            }
    }

    suspend fun updateSession(requestUpdateDto: RequestUpdateSessionDto): EmptyResult<DataError.Remote> {
        return try {
            val json = Json { encodeDefaults = false }
            val request = json.encodeToString(requestUpdateDto)
            webSocketSession?.send(Frame.Text(request))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Remote.UNKNOWN)
        }
    }

    suspend fun closeWebsocketSession() {
        webSocketSession?.close(
            CloseReason(CloseReason.Codes.SERVICE_RESTART, "")
        )
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

private const val TAG = "RealtimeRemoteDataSource"