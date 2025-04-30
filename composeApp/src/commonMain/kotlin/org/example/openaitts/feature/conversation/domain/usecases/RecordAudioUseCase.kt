package org.example.openaitts.feature.conversation.domain.usecases

import org.example.openaitts.feature.audio.AudioFileManager
import org.example.openaitts.feature.audio.AudioRecorder

class RecordAudioUseCase(
    private val audioRecorder: AudioRecorder,
    private val audioFileManager: AudioFileManager,
) {
    fun execute() {
        audioFileManager.stop()
        audioRecorder.record()
    }
}