package org.example.openaitts.feature.conversation.domain

import kotlinx.coroutines.flow.Flow
import org.example.openaitts.core.data.MODEL_REALTIME
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.conversation.data.ConversationRemoteDataSource
import org.example.openaitts.feature.conversation.data.dto.SessionRequestDto

class ConversationUseCase(
    private val remoteDataSource: ConversationRemoteDataSource,
) {
    suspend fun establishConnection(): Result<Flow<String>, DataError.Remote> {
        remoteDataSource.createSession(SessionRequestDto(model = MODEL_REALTIME))
        return remoteDataSource.establishWebSocketConnection()
    }
}