package org.example.openaitts.feature.conversation.domain.usecases

import org.example.openaitts.feature.tts.domain.AudioFileManager

class AudioPlaybackUseCase(
    private val audioFileManager: AudioFileManager,
) {
    fun play() = audioFileManager.play()

    fun stop() = audioFileManager.stop()
}