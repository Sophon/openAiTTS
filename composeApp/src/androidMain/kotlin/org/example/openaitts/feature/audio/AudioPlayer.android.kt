package org.example.openaitts.feature.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

actual class AudioPlayer(private val context: Context) {
    private val audioTrack = createAudioTrack()
    private val audioChannel = Channel<ByteArray>(capacity = Channel.UNLIMITED)
    private var streamingJob: Job? = null
    private val myData: MutableList<ByteArray> = mutableListOf()

    @OptIn(DelicateCoroutinesApi::class)
    actual fun cache(data: ByteArray) {
        if (audioChannel.isClosedForSend.not()) {
            Napier.d(tag = TAG) { "caching data" }
            audioChannel.trySend(data)
            myData.add(data)
        } else {
            Napier.d(tag = TAG) { "channel closed, cannot cache" }
        }
    }

    actual fun play() {
        if (audioTrack.state != AudioTrack.STATE_INITIALIZED) {
            Napier.e(tag = TAG) { "not initialized" }
        } else {
            audioTrack.play()

            streamingJob = CoroutineScope(Dispatchers.IO).launch {
                Napier.d(tag = TAG) { "writing from channel to audiotrack" }
                for (chunk in audioChannel) {
                    audioTrack.write(chunk, 0, chunk.size)
                }
            }
        }
    }

    actual fun stop() {
        streamingJob?.cancel()
        audioTrack.apply {
            pause()
            flush()
        }

        clearChannel()
    }

    actual fun retrieveFile(path: String): ByteArray? {
        try {
            return File(context.cacheDir, "snow.wav").readBytes()
//            return File(context.cacheDir, path).readBytes()
        } catch (e: Exception) {
            Napier.e(tag = TAG) { "Error: ${e.message}" }
            return null
        }
    }

    private fun createAudioTrack(): AudioTrack {
        val channelConfig = AudioFormat.CHANNEL_OUT_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, channelConfig, audioFormat)

        return AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
            AudioFormat.Builder()
                .setEncoding(audioFormat)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(channelConfig)
                .build(),
            bufferSize,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun clearChannel() {
        while (audioChannel.isEmpty.not()) {
            audioChannel.tryReceive().getOrNull()
        }
    }
}

private const val TAG = "AudioPlayer"
private const val SAMPLE_RATE = 24_000