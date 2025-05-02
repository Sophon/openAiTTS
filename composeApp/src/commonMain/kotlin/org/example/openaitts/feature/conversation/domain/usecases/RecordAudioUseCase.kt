package org.example.openaitts.feature.conversation.domain.usecases

import org.example.openaitts.feature.audio.AudioPlayer
import org.example.openaitts.feature.audio.AudioRecorder

class RecordAudioUseCase(
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer,
) {
    fun execute() {
//        audioPlayer.stop()
        audioRecorder.record()
    }
}