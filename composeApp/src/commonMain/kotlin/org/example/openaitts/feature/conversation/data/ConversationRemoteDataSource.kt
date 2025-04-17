package org.example.openaitts.feature.conversation.data

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import org.example.openaitts.core.data.BASE_URL
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.EmptyResult
import org.example.openaitts.core.domain.Result
import org.example.openaitts.core.network.safeCall
import org.example.openaitts.feature.conversation.data.dto.MessageDto
import org.example.openaitts.feature.conversation.data.dto.SessionRequestDto
import org.example.openaitts.feature.conversation.data.dto.SessionResponseDto

class ConversationRemoteDataSource(
    private val httpClient: HttpClient,
) {
    private lateinit var session: SessionResponseDto
    private var webSocketSession: WebSocketSession? = null

    suspend fun createSession(requestDto: SessionRequestDto): EmptyResult<DataError.Remote> {
        initializeSession(requestDto).let { if (it is Result.Error) return it }

        return Result.Success(Unit)
    }

    private suspend fun initializeSession(requestDto: SessionRequestDto): EmptyResult<DataError.Remote> {
        val response: Result<SessionResponseDto, DataError.Remote> = safeCall {
            httpClient.post(SESSION_URL) { setBody(requestDto) }
        }

        val result = when (response) {
            is Result.Success -> {
                session = response.data
                Result.Success(Unit)
            }
            is Result.Error -> {
                Napier.e { response.error.toString() }
                Result.Error(response.error)
            }
        }

        httpClient.close()
        return result
    }

    suspend fun establishWebSocketConnection(): Result<Flow<String>, DataError.Remote> {
        val url = REALTIME_WEBSOCKET_URL.replace("SESSION_ID", session.id)
        webSocketSession = httpClient.webSocketSession(urlString = url)

        Napier.d(tag = "ktor") { "webSocketSession: $webSocketSession" }

        return if (webSocketSession == null) {
            Result.Error(DataError.Remote.UNKNOWN)
        } else {
            val flow = flow {
                val messages = webSocketSession!!
                    .incoming
                    .consumeAsFlow()
                    .filterIsInstance<Frame.Text>()
                    .mapNotNull {
                        it.readText()
                    }

                emitAll(messages)
            }
            Result.Success(flow)
        }
    }

    suspend fun send(message: MessageDto): EmptyResult<DataError.Remote> {
        try {
            webSocketSession?.outgoing?.send(Frame.Text(message.text))
            return Result.Success(Unit)
        } catch (e: Exception) {
            Napier.e(tag = TAG) { e.message.toString() }
            return Result.Error(DataError.Remote.UNKNOWN)
        }
    }
}

private const val SESSION_URL = "$BASE_URL/realtime/sessions"
private const val REALTIME_WEBSOCKET_URL = "wss://api.openai.com/v1/realtime/sessions/SESSION_ID/stream"
private const val TAG = "ConversationRemoteDataSource"