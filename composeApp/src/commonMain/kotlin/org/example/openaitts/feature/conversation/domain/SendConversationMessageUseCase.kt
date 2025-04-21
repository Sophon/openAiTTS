package org.example.openaitts.feature.conversation.domain

import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.EmptyResult
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.conversation.data.RealtimeRemoteDataSource
import org.example.openaitts.feature.conversation.data.dto.CreateResponseRequestDto
import org.example.openaitts.feature.conversation.data.toDto

class SendConversationMessageUseCase(
    private val remoteDataSource: RealtimeRemoteDataSource,
) {
    suspend fun sendMessage(message: String): EmptyResult<DataError.Remote> {
        val messageObject = Message(
            type = Message.Type.MESSAGE,
            role = Message.Role.USER,
            content = listOf(
                Message.Content(
                    type = Message.Content.Type.INPUT_TEXT,
                    text = message,
                )
            )
        ).toDto()

        return when (val response = remoteDataSource.send(messageObject)) {
            is Result.Success -> {
                val request = CreateResponseRequestDto(
                    response = CreateResponseRequestDto.Response(
                        modalities = listOf(CreateResponseRequestDto.Response.Modality.TEXT)
                    )
                )
                remoteDataSource.requestResponse(request)
            }
            is Result.Error -> response
        }
    }
}