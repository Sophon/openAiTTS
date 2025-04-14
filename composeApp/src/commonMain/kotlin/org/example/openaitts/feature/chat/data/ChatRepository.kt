package org.example.openaitts.feature.chat.data

import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.chat.data.dto.ChatResponseDto

interface ChatRepository {
    suspend fun sendMessage(message: String): Result<ChatResponseDto, DataError.Remote>
}