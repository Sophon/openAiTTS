package org.example.openaitts.feature.conversation.domain.usecases

import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.EmptyResult
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.audio.AudioFileManager
import org.example.openaitts.feature.audio.AudioRecorder
import org.example.openaitts.feature.conversation.data.RealtimeRemoteDataSource
import org.example.openaitts.feature.conversation.data.dto.RequestCreateItemDto
import org.example.openaitts.feature.conversation.data.dto.RequestResponseDto
import org.example.openaitts.feature.conversation.domain.models.Content
import org.example.openaitts.feature.conversation.domain.models.EventType
import org.example.openaitts.feature.conversation.domain.models.MessageItem
import org.example.openaitts.feature.conversation.domain.models.Modality
import org.example.openaitts.feature.conversation.domain.models.Role
import org.example.openaitts.feature.conversation.domain.utils.encode

class SendConversationMessageUseCase(
    private val remoteDataSource: RealtimeRemoteDataSource,
    private val audioFileManager: AudioFileManager,
    private val audioRecorder: AudioRecorder,
) {
    private var eventId: String? = null

    suspend fun sendTextMessage(
        message: String,
        useAudio: Boolean = false
    ): EmptyResult<DataError.Remote> {
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
            is Result.Success -> requestResponse(useAudio)
            is Result.Error -> response
        }
    }

    suspend fun sendVoiceMessage(): EmptyResult<DataError.Remote> {
        audioRecorder.location?.let { recordingFilePath ->
            audioFileManager.retrieveFile(recordingFilePath)?.let { data ->
                val requestDto = RequestCreateItemDto(
                    type = EventType.ITEM_CREATE,
                    item = MessageItem(
                        type = MessageItem.Type.MESSAGE,
                        role = Role.USER,
                        content = listOf(
                            Content(type = Content.Type.INPUT_AUDIO, audio = data.encode())
                        )
                    )
                )

                return when (val response = remoteDataSource.send(requestDto)) {
                    is Result.Success -> {
//                        when (val result = transcriptionRemoteDataSource.sendAudio(
//                            TranscriptionRequestDto(file = data)
//                        )) {
//                            is Result.Success -> {
//                                Napier.d(tag = TAG) { "received transcription ${result.data}" }
//                            }
//                            is Result.Error -> Napier.e { result.error.toString() }
//                        }

                        requestResponse(true)
                    }
                    is Result.Error -> response
                }
            }
        }

        return Result.Error(DataError.Remote.UNKNOWN)
    }

    private suspend fun requestResponse(useAudio: Boolean): EmptyResult<DataError.Remote> {
        val requestResponseDto = RequestResponseDto(
            response = RequestResponseDto.Response(
                modalities = if (useAudio) {
                    listOf(Modality.TEXT, Modality.AUDIO)
                } else {
                    listOf(Modality.TEXT)
                }
            )
        )

        return remoteDataSource.requestResponse(requestResponseDto)
    }
}

private const val TAG = "SendConversationMessageUseCase"