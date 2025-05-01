package org.example.openaitts.feature.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import io.github.aakira.napier.Napier
import java.io.File

actual class AudioFileManager(private val context: Context) {
    private var cachedData: ByteArray? = null
    private var audioTrack: AudioTrack? = null

    actual fun cache(data: ByteArray) {
        cachedData = if (cachedData == null) {
            data
        } else {
            cachedData!! + data
        }
    }

    actual fun play() {
        cachedData?.let { pcmData ->
            createAudioTrack(size = pcmData.size).apply {
                audioTrack = this
                write(pcmData, 0, pcmData.size)
                play()
            }
        }
    }

    actual fun stop() {
        audioTrack?.stop()
    }

    actual fun retrieveFile(path: String): ByteArray? {
        try {
//            return File(context.cacheDir, "snow.wav").readBytes()
            return File(context.cacheDir, path).readBytes()
        } catch (e: Exception) {
            Napier.e(tag = TAG) { "Error: ${e.message}" }
            return null
        }
    }

    actual fun testPlay() {
        File(context.cacheDir, "test.wav").let { file ->
            val pcmData = file.readBytes()

            createAudioTrack(pcmData.size).apply {
                write(pcmData, 0, pcmData.size)
                play()
            }
        }
    }

    private fun createAudioTrack(size: Int = DEFAULT_TRACK_SIZE): AudioTrack {
        return AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(24_000)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            size,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
    }
}

private const val TAG = "AudioFileManager"
private const val DEFAULT_TRACK_SIZE = 10_000_000