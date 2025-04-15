package org.example.openaitts.feature.tts.domain

import io.github.aakira.napier.Napier
import org.example.openaitts.core.data.MODEL_TTS
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.tts.data.TtsRemoteDataSource
import org.example.openaitts.feature.tts.data.dto.TtsRequestDto

class PromptTTSUseCase(
    private val dataSource: TtsRemoteDataSource,
) {
    suspend fun execute(message: String): Result<ByteArray, DataError.Remote> {
        val body = TtsRequestDto(
            model = MODEL_TTS,
            input = message,
            voice = "nova", //TODO: various voices
        )

        return dataSource.sendMessage(body)
    }
}