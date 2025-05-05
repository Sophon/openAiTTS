package org.example.openaitts.feature.conversation.domain.usecases

import io.github.aakira.napier.Napier
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.audio.AudioPlayer
import org.example.openaitts.feature.conversation.data.RealtimeRemoteDataSource
import org.example.openaitts.feature.conversation.data.dto.RequestUpdateSessionDto
import org.example.openaitts.feature.conversation.data.dto.ResponseDto
import org.example.openaitts.feature.conversation.domain.models.Content
import org.example.openaitts.feature.conversation.domain.models.EventType
import org.example.openaitts.feature.conversation.domain.models.MessageItem
import org.example.openaitts.feature.conversation.domain.models.Role
import org.example.openaitts.feature.conversation.domain.models.Session
import org.example.openaitts.feature.conversation.domain.utils.decode

class ConversationUseCase(
    private val remoteDataSource: RealtimeRemoteDataSource,
    private val audioPlayer: AudioPlayer,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var playbackStarted = false

    suspend fun establishConnection(): Flow<Result<MessageItem, DataError.Remote>> {
        remoteDataSource.closeWebsocketSession()

        val flow = remoteDataSource.initializeWebSocketSession(
            processText = ::processText,
            processBinary = ::processBinary,
        ).map { dto ->
            when {
                (dto.delta != null) -> {
                    Napier.d(tag = TAG) { "delta" }
                    Result.Success(createMessageItemFromDelta(dto))
                }
                (dto.item == null) -> {
                    Result.Error(DataError.Remote.UNKNOWN)
                }
                else -> {
                    Result.Success(dto.item)
                }
            }
        }

        updateToAudioTranscription()

        return flow
    }

    //returning null means we're not handling it
    private fun processText(textFrame: Frame.Text): ResponseDto? {
        val text = textFrame.readText()
        val eventObject = json.decodeFromString<ResponseDto>(text)

        return when (eventObject.type) {
            EventType.RESPONSE_OUTPUT_ITEM_DONE -> {
                Napier.d(tag = TAG) { "response output item done: ${eventObject.item?.content?.firstOrNull()?.text}" }
                eventObject
            }
            EventType.RESPONSE_AUDIO_DELTA -> {
                Napier.d(tag = TAG) { "received audio chunk" }
                eventObject.delta?.let { rawData ->
                    audioPlayer.apply {
                        cache(rawData.decode())
                        if (playbackStarted.not()) {
                            play()
                            playbackStarted = true
                        }
                    }
                }
                null
            }
            EventType.ITEM_CREATED -> {
                Napier.d(tag = TAG) { "item created: $eventObject" }
                null
            }
            EventType.RESPONSE_AUDIO_DONE -> {
                Napier.d(tag = TAG) { "audio chunks done" }
                eventObject
            }
            EventType.RESPONSE_AUDIO_TRANSCRIPT_DELTA -> {
                Napier.d(tag = TAG) { "received a chunk of the audio transcript" }
                eventObject
            }
            EventType.RESPONSE_AUDIO_TRANSCRIPT_DONE -> {
                Napier.d(tag = TAG) { "audio transcript done" }
                null
            }
            EventType.SESSION_UPDATED -> {
                Napier.d(tag = TAG) { "session updated: $eventObject" }
                null
            }
            EventType.ERROR -> {
                Napier.e(tag = TAG) { "error: $eventObject" }
                null
            }
            else -> {
                Napier.d(tag = TAG) { "unhandled event: $eventObject" }
                null
            }
        }
    }

    private fun processBinary(binaryFrame: Frame.Binary): ResponseDto? {
        Napier.d(tag = TAG) { "received binary frame; not handled" }

        return null
    }

    private fun createMessageItemFromDelta(dto: ResponseDto): MessageItem {
        return MessageItem(
            type = MessageItem.Type.MESSAGE,
            role = Role.ASSISTANT,
            content = listOf(
                Content(
                    type = Content.Type.TEXT,
                    text = dto.delta,
                )
            ),
            isIncomplete = true,
        )
    }

    private suspend fun updateToAudioTranscription() {
        val dto = RequestUpdateSessionDto(
            type = EventType.SESSION_UPDATE,
            session = Session(
                inputAudioTranscription = Session.InputAudioTranscription(
                    language = "en",
                    model = "whisper-1",
                    prompt = "Use a British accent.",
                ),
            )
        )

        remoteDataSource.updateSession(dto)
    }
}

private const val TAG = "ConversationUseCase"