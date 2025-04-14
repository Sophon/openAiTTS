package org.example.openaitts.feature.chat.domain

import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.core.domain.map
import org.example.openaitts.feature.chat.data.remote.ChatRemoteDataSource

class SendMessageUseCase(
    private val dataSource: ChatRemoteDataSource,
) {
    suspend fun execute(message: Message): Result<Message, DataError.Remote> {
        return dataSource.sendMessage(message).map { dto ->
            Message(role = dto.role, content = dto.content)
        }
    }
}