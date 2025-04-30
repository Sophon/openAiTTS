package org.example.openaitts.feature.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import okio.buffer
import okio.sink
import java.io.File

actual class AudioFileManager(private val context: Context) {
    private var file: File? = null
    private var cachedData: ByteArray? = null
    val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    val audioFormat = AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setSampleRate(16_000)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .build()

    actual fun save(data: ByteArray) {
        file = File(context.filesDir, FILENAME).also { file ->
            file.sink().buffer().use { it.write(data) }
        }
    }

    actual fun cache(data: ByteArray) {
        cachedData = if (cachedData == null) {
            data
        } else {
            cachedData!! + data
        }
    }

    actual fun saveCached() {
        cachedData?.let { data ->
            save(data)
            cachedData = null
        }
    }

    actual fun play() {
        file?.let { file ->
            val pcmData = file.readBytes()

            val track = AudioTrack(
                audioAttributes,
                audioFormat,
                pcmData.size,
                AudioTrack.MODE_STATIC,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            track.write(pcmData, 0, pcmData.size)
            track.play()
        }
    }

    actual fun stop() {
        //TODO: implement
    }
}

private const val FILENAME = "tts.pcm"