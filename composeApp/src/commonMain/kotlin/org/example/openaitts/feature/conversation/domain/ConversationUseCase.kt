package org.example.openaitts.feature.conversation.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.conversation.data.RealtimeRemoteDataSource
import org.example.openaitts.feature.conversation.domain.models.MessageItem

class ConversationUseCase(
    private val remoteDataSource: RealtimeRemoteDataSource,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun establishConnection(): Flow<Result<MessageItem, DataError.Remote>> {
        return remoteDataSource.initializeSession().flatMapLatest { dto ->
            flow {
                emit(
                    if (dto.item == null) {
                        Result.Error(DataError.Remote.UNKNOWN)
                    } else {
                        Result.Success(dto.item)
                    }
                )
            }
        }
    }
}