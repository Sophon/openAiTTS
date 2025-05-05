package org.example.openaitts.feature.transcription

import io.github.aakira.napier.Napier
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.audio.AudioPlayer
import org.example.openaitts.feature.audio.AudioRecorder

class TranscribeAudioMessageUseCase(
    private val transcriptionRemoteDataSource: TranscriptionRemoteDataSource,
    private val audioPlayer: AudioPlayer,
    private val audioRecorder: AudioRecorder,
) {
    suspend fun execute(): Result<String, DataError.Remote> {
        audioRecorder.location?.let { fileName ->
            audioPlayer.retrieveFile(fileName)?.let { data ->
                return when (val result = transcriptionRemoteDataSource.transcribeAudioFile(fileName, data)) {
                    is Result.Success -> {
                        Napier.d(tag = TAG) { "received ${result.data}" }
                        Result.Success(result.data.text)
                    }
                    is Result.Error -> {
                        Napier.e(tag = TAG) { result.error.toString() }
                        Result.Error(result.error)
                    }
                }
            }
        }

        return Result.Error(DataError.Remote.UNKNOWN)
    }
}

private const val TAG = "TranscribeAudioMessageUseCase"