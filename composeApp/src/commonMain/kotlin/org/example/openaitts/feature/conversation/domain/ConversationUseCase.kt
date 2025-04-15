package org.example.openaitts.feature.conversation.domain

import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.core.domain.map
import org.example.openaitts.feature.conversation.data.ConversationRemoteDataSource

class ConversationUseCase(
    private val remoteDataSource: ConversationRemoteDataSource,
) {
    suspend fun startSession(): Result<Session, DataError.Remote> {
        return remoteDataSource.createSession().map { it.toDomain() }
    }
}