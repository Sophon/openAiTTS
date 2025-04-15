package org.example.openaitts.feature.tts.domain

import android.media.MediaPlayer

actual object AudioPlayer {
    private var player: MediaPlayer? = null

    actual fun play(path: String) {
        player = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            start()
        }
    }

    actual fun stop() {
        player?.apply {
            stop()
            release()
        }
        player = null
    }
}