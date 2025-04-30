package org.example.openaitts.feature.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import io.github.aakira.napier.Napier
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
    var audioTrack: AudioTrack? = null

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

            audioTrack = AudioTrack(
                audioAttributes,
                audioFormat,
                pcmData.size,
                AudioTrack.MODE_STATIC,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            audioTrack!!.write(pcmData, 0, pcmData.size)
            audioTrack!!.play()
        }
    }

    actual fun stop() {
        audioTrack?.stop()
    }

    actual fun retrieveFile(path: String): ByteArray? {
        try {
            return File(context.cacheDir, "snow.wav").readBytes()
        } catch (e: Exception) {
            Napier.e(tag = TAG) { "Error: ${e.message}" }
            return null
        }
    }

    actual fun testPlay() {
        File(context.cacheDir, "test.wav").let { file ->
            val pcmData = file.readBytes()

            AudioTrack(
                audioAttributes,
                audioFormat,
                pcmData.size,
                AudioTrack.MODE_STATIC,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            ).apply {
                write(pcmData, 0, pcmData.size)
                play()
            }
        }
    }
}

private const val FILENAME = "tts.pcm"
private const val TAG = "AudioFileManager"