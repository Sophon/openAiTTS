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
import org.example.openaitts.feature.conversation.data.dto.MessageDto

class RealtimeRemoteDataSource(
    private val httpClient: HttpClient,
) {
    private var webSocketSession: WebSocketSession? = null

    suspend fun initializeSession(): Flow<String> {
        val url = "$REALTIME_WEBSOCKET_URL?model=$MODEL_REALTIME"
        webSocketSession = httpClient.webSocketSession(url) {
            headers { append("openai-beta", "realtime=v1") }
        }

        return flow {
            val messages = webSocketSession!!
                .incoming
                .consumeAsFlow()
                .mapNotNull {
                    when (it) {
                        is Frame.Text -> it.readText()
                        is Frame.Binary -> "binary"
                        else -> "else"
                    }
                }

            emitAll(messages)
        }
    }

    suspend fun send(message: MessageDto): EmptyResult<DataError.Remote> {
        try {
            val textMessage = Json.encodeToString(message)
            webSocketSession?.send(Frame.Text(textMessage))
            return Result.Success(Unit)
        } catch (e: Exception) {
            Napier.e(tag = TAG) { e.message.toString() }
            return Result.Error(DataError.Remote.UNKNOWN)
        }
    }
}

private const val TAG = "ConversationRemoteDataSource"