package org.example.openaitts.feature.tts.domain

import android.content.Context
import android.media.MediaPlayer
import okio.buffer
import okio.sink
import java.io.File

actual class AudioFileManager(private val context: Context) {
    private var player: MediaPlayer? = null
    private var file: File? = null

    actual fun save(data: ByteArray) {
        file = File(context.filesDir, FILENAME).also { file ->
            file.sink().buffer().use { it.write(data) }
        }
    }

    actual fun play() {
        file?.let {
            player?.release()
            player = MediaPlayer().apply {
                setDataSource(it.absolutePath)
                prepare()
                start()
            }
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

private const val FILENAME = "tts.mp3"