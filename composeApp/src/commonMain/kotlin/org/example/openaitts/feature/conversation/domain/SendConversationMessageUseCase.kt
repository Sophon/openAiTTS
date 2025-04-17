package org.example.openaitts.feature.conversation.domain

import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.EmptyResult
import org.example.openaitts.feature.conversation.data.ConversationRemoteDataSource
import org.example.openaitts.feature.conversation.data.dto.MessageDto

class SendConversationMessageUseCase(
    private val remoteDataSource: ConversationRemoteDataSource,
) {
    suspend fun sendMessage(message: String): EmptyResult<DataError.Remote> {
        return remoteDataSource.send(MessageDto(text = message))
    }
}