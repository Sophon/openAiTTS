package org.example.openaitts.feature.conversation.domain.usecases

import org.example.openaitts.feature.audio.AudioRecorder

class StopAudioRecordingUseCase(
    private val audioRecorder: AudioRecorder,
) {
    fun execute() = audioRecorder.stopRecording()
}