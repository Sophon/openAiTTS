package org.example.openaitts.feature.conversation.domain

import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.EmptyResult
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.conversation.data.RealtimeRemoteDataSource
import org.example.openaitts.feature.conversation.data.dto.RequestCreateItemDto
import org.example.openaitts.feature.conversation.data.dto.RequestResponseDto
import org.example.openaitts.feature.conversation.domain.models.Content
import org.example.openaitts.feature.conversation.domain.models.EventType
import org.example.openaitts.feature.conversation.domain.models.MessageItem
import org.example.openaitts.feature.conversation.domain.models.Modality
import org.example.openaitts.feature.conversation.domain.models.Role

class SendConversationMessageUseCase(
    private val remoteDataSource: RealtimeRemoteDataSource,
) {
    suspend fun sendTextMessage(message: String): EmptyResult<DataError.Remote> {
        val requestCreateItemDto = RequestCreateItemDto(
            type = EventType.ITEM_CREATE,
            item = MessageItem(
                type = MessageItem.Type.MESSAGE,
                role = Role.USER,
                content = listOf(
                    Content(type = Content.Type.INPUT_TEXT, text = message)
                )
            )
        )

        return when (val response = remoteDataSource.send(requestCreateItemDto)) {
            is Result.Success -> {
                val requestResponseDto = RequestResponseDto(
                    response = RequestResponseDto.Response(
                        modalities = listOf(Modality.TEXT)
                    )
                )

                remoteDataSource.requestResponse(requestResponseDto)
            }
            is Result.Error -> response
        }
    }

    suspend fun sendVoiceMessage(audio: ByteArray): EmptyResult<DataError.Remote> {
        TODO("implement")
    }
}