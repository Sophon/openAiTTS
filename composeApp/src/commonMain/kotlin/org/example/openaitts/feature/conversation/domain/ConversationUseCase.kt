package org.example.openaitts.feature.conversation.domain

import org.example.openaitts.core.data.MODEL_REALTIME
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.core.domain.map
import org.example.openaitts.feature.conversation.data.ConversationRemoteDataSource
import org.example.openaitts.feature.conversation.data.dto.SessionRequestDto

class ConversationUseCase(
    private val remoteDataSource: ConversationRemoteDataSource,
) {
    suspend fun startSession(): Result<Session, DataError.Remote> {
        val body = SessionRequestDto(model = MODEL_REALTIME)

        return remoteDataSource.createSession(body).map { it.toDomain() }
    }
}