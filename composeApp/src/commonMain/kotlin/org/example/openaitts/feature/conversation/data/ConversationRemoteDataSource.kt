package org.example.openaitts.feature.conversation.data

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
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

    suspend fun createSession(requestDto: SessionRequestDto): EmptyResult<DataError.Remote> {
        initializeSession(requestDto).let { if (it is Result.Error) return it }
        establishWebSocketConnection()

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

    private suspend fun establishWebSocketConnection() {
        val url = REALTIME_WEBSOCKET_URL.replace("SESSION_ID", session.id)
        httpClient.webSocket(url) {
            val message = MessageDto(text = "are you ready?")
            send(Frame.Text(message.toString()))

            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        Napier.d(frame.readText(), tag = TAG)
                    }
                    is Frame.Binary -> Napier.d("binary received", tag = TAG)
                    else -> Unit
                }
            }
        }
    }

    //TODO: send message
}

private const val SESSION_URL = "$BASE_URL/realtime/sessions"
private const val REALTIME_WEBSOCKET_URL = "wss://api.openai.com/v1/realtime/sessions/SESSION_ID/stream"
private const val TAG = "ConversationRemoteDataSource"