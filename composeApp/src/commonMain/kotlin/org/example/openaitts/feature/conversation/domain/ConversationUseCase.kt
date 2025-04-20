package org.example.openaitts.feature.conversation.domain

import kotlinx.coroutines.flow.Flow
import org.example.openaitts.feature.conversation.data.RealtimeRemoteDataSource

class ConversationUseCase(
    private val remoteDataSource: RealtimeRemoteDataSource,
) {
    suspend fun establishConnection(): Flow<String> {
        return remoteDataSource.initializeSession()
    }
}