package org.example.openaitts.feature.tts.data

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.example.openaitts.core.data.BASE_URL
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.core.network.safeCall
import org.example.openaitts.feature.tts.data.dto.TtsRequestDto

interface TtsRemoteDataSource {
    suspend fun sendMessage(request: TtsRequestDto): Result<ByteArray, DataError.Remote>
}

class TtsRemoteDataSourceImpl(
    private val httpClient: HttpClient,
): TtsRemoteDataSource {
    override suspend fun sendMessage(request: TtsRequestDto): Result<ByteArray, DataError.Remote> {
        return safeCall { httpClient.post(TTS_URL) { setBody(request) } }
    }
}

private const val TTS_URL = "$BASE_URL/audio/speech"