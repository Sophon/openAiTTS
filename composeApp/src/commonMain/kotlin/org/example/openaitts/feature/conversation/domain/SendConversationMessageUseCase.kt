package org.example.openaitts.feature.conversation.domain

import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.EmptyResult
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.conversation.data.RealtimeRemoteDataSource
import org.example.openaitts.feature.conversation.data.dto.ConversationEventDto

class SendConversationMessageUseCase(
    private val remoteDataSource: RealtimeRemoteDataSource,
) {
    suspend fun sendTextMessage(message: String): EmptyResult<DataError.Remote> {
        val eventObject = ConversationEventDto(
            type = ConversationEventDto.Type.ITEM_CREATE,
            item = ConversationEventDto.Item(
                type = ConversationEventDto.Item.Type.MESSAGE,
                role = ConversationEventDto.Item.Role.USER,
                content = listOf(
                    ConversationEventDto.Item.Content(
                        type = ConversationEventDto.Item.Content.Type.INPUT_TEXT,
                        text = message,
                    )
                )
            ),
        )

        return when (val response = remoteDataSource.send(eventObject)) {
            is Result.Success -> {
                val request = ConversationEventDto(
                    type = ConversationEventDto.Type.RESPONSE_CREATE,
                    response = ConversationEventDto.Item(
                        modalities = listOf(ConversationEventDto.Item.Modality.TEXT)
                    )
                )
                remoteDataSource.requestResponse(request)
            }
            is Result.Error -> response
        }
    }

    suspend fun sendVoiceMessage(audio: ByteArray): EmptyResult<DataError.Remote> {
        TODO("implement")
    }
}