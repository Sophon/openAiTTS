package org.example.openaitts.feature.conversation.domain

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
import org.example.openaitts.feature.conversation.data.RealtimeRemoteDataSource
import org.example.openaitts.feature.conversation.data.dto.ResponseDto
import org.example.openaitts.feature.conversation.domain.models.EventType
import org.example.openaitts.feature.conversation.domain.models.MessageItem
import org.example.openaitts.feature.tts.domain.AudioFileManager

class ConversationUseCase(
    private val remoteDataSource: RealtimeRemoteDataSource,
    private val audioFileManager: AudioFileManager,
) {
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun establishConnection(): Flow<Result<MessageItem, DataError.Remote>> {
        return remoteDataSource.initializeSession(
            processText = ::processText,
            processBinary = ::processBinary,
        ).flatMapLatest { dto ->
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

    private fun processText(textFrame: Frame.Text): ResponseDto? {
        val text = textFrame.readText()
        val eventObject = json.decodeFromString<ResponseDto>(text)

        return when (eventObject.type) {
            EventType.RESPONSE_OUTPUT_ITEM_DONE -> {
                Napier.d(tag = TAG) {
                    "response output item done: ${eventObject.item?.content?.firstOrNull()?.text}"
                }
                eventObject
            }
            EventType.RESPONSE_AUDIO_DELTA -> {
                Napier.d(tag = TAG) { "received audio chunk" }
                eventObject.delta?.let { audioFileManager.cache(it.encodeToByteArray()) }
                eventObject
            }
            EventType.RESPONSE_AUDIO_DONE -> {
                Napier.d(tag = TAG) { "audio chunks done" }
                audioFileManager.saveCached()
                audioFileManager.play()
                null
            }
//            EventType.RESPONSE_AUDIO_TRANSCRIPT_DELTA -> {
//                Napier.d(tag = TAG) { "received audio transcript chunk" }
//                null
//            }
//            EventType.RESPONSE_AUDIO_TRANSCRIPT_DONE -> {
//                Napier.d(tag = TAG) { "audio transcript done" }
//                null
//            }
            EventType.ERROR -> {
                Napier.e(tag = TAG) { "error: ${eventObject.error?.message}" }
                null
            }
            else -> {
                Napier.d(tag = TAG) { "some other event: ${eventObject.type}" }
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