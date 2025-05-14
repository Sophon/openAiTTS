package org.example.openaitts.feature.conversation.domain.usecases

import org.example.openaitts.feature.audio.AudioRecorder

class RecordAudioUseCase(
    private val audioRecorder: AudioRecorder,
) {
    fun execute() {
        audioRecorder.record()
    }
}