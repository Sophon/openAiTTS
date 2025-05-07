package org.example.openaitts.feature.transcription

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import org.example.openaitts.core.data.BASE_URL
import org.example.openaitts.core.data.TRANSCRIPTION_URL
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.core.network.safeCall

interface TranscriptionRemoteDataSource {
    suspend fun transcribeAudioFile(fileName: String, audioFile: ByteArray): Result<Transcription, DataError.Remote>
}

class TranscriptionRemoteDataSourceImpl(
    private val client: HttpClient,
): TranscriptionRemoteDataSource {
    override suspend fun transcribeAudioFile(fileName: String, audioFile: ByteArray): Result<Transcription, DataError.Remote> {
        return safeCall {
            client.submitFormWithBinaryData(
                url = URL,
                formData = formData {
                    append(
                        key = "model",
                        value = "whisper-1",
                    )
                    append(
                        key = "file",
                        value = audioFile,
                        headers = Headers.build {
                            append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"$fileName\"")
                            append(HttpHeaders.ContentType, "audio/wav")
                        }
                    )
                }
            ).apply {
                val result = bodyAsText()
                Napier.d(tag = TAG) { "response: $result" }
            }
        }
    }
}

private const val URL = BASE_URL + TRANSCRIPTION_URL
private const val TAG = "TranscriptionRemoteDataSource"