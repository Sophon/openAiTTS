package org.example.openaitts.feature.chat.domain

import org.example.openaitts.core.data.MODEL
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.core.domain.map
import org.example.openaitts.feature.chat.data.dto.ChatRequestDto
import org.example.openaitts.feature.chat.data.remote.ChatRemoteDataSource

class SendMessageUseCase(
    private val dataSource: ChatRemoteDataSource,
) {
    suspend fun execute(message: Message): Result<Message, DataError.Remote> {
        val body = ChatRequestDto(
            model = MODEL,
            messages = listOf(ChatRequestDto.Message(role = "user", content = message.content)),
        )
        return dataSource.sendMessage(body).map { dto ->
            var role = ""
            var content = ""
            dto.choices.firstOrNull()?.message?.let {
                role = it.role
                content = it.content
            }
            Message(role = role, content = content)
        }
    }
}