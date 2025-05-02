package org.example.openaitts.feature.conversation.domain.usecases

import org.example.openaitts.feature.audio.AudioPlayer

class AudioPlaybackUseCase(
    private val audioPlayer: AudioPlayer,
) {
    fun play() = audioPlayer.play()

    fun stop() = audioPlayer.stop()
}