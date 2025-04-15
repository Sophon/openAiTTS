package org.example.openaitts.feature.tts.domain

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

class AudioFileManager(
    private val audioPlayer: AudioPlayer,
) {
    fun save(data: ByteArray) {
        FileSystem.SYSTEM.write(FILENAME.toPath()) {
            write(data)
        }
    }

    fun play() {
        audioPlayer.play(FILENAME)
    }

    fun stop() {
        audioPlayer.stop()
    }
}

private const val FILENAME = "tts.mp3"