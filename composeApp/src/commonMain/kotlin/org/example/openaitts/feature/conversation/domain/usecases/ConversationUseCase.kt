package org.example.openaitts.feature.conversation.domain.usecases

import io.github.aakira.napier.Napier
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.audio.AudioPlayer
import org.example.openaitts.feature.conversation.data.RealtimeRemoteDataSource
import org.example.openaitts.feature.conversation.data.dto.ResponseDto
import org.example.openaitts.feature.conversation.domain.models.EventType
import org.example.openaitts.feature.conversation.domain.models.MessageItem
import org.example.openaitts.feature.conversation.domain.utils.decode

class ConversationUseCase(
    private val remoteDataSource: RealtimeRemoteDataSource,
    private val audioPlayer: AudioPlayer,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var playbackStarted = false

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun establishConnection(): Flow<Result<MessageItem, DataError.Remote>> {
        remoteDataSource.closeWebsocketSession()

        val flowResponse = remoteDataSource.initializeWebSocketSession(
            processText = ::processText,
            processBinary = ::processBinary,
        )

        return flowResponse.flatMapLatest { dto ->
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
            EventType.RESPONSE_AUDIO_DONE -> {
                Napier.d(tag = TAG) { "audio chunks done" }
                eventObject
            }
            EventType.RESPONSE_AUDIO_TRANSCRIPT_DELTA -> {
                Napier.d(tag = TAG) { "received a chunk of the audio transcript" }
                null
            }
            EventType.RESPONSE_AUDIO_TRANSCRIPT_DONE -> {
                Napier.d(tag = TAG) { "audio transcript done" }
                //TODO: pass the incomplete dto to the viewmodel
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
}

private const val TAG = "ConversationUseCase"