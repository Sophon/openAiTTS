package org.example.openaitts.feature.chat.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.example.openaitts.core.data.BASE_URL
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.core.network.safeCall
import org.example.openaitts.feature.chat.data.dto.ChatResponseDto
import org.example.openaitts.feature.chat.domain.Message

interface ChatRemoteDataSource {
    suspend fun sendMessage(message: Message): Result<ChatResponseDto, DataError.Remote>
}

class ChatRemoteDataSourceImpl(
    private val httpClient: HttpClient
): ChatRemoteDataSource {
    override suspend fun sendMessage(message: Message): Result<ChatResponseDto, DataError.Remote> {
        return safeCall {
            httpClient.post(BASE_URL) {
                setBody(
                    mapOf(
                        "model" to "gpt-4-turbo",
                        "role" to message.role,
                        "content" to message.content,
                    )
                )
            }
        }
    }
}