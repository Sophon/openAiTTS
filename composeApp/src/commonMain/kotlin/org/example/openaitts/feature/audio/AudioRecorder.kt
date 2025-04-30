package org.example.openaitts.feature.audio

import dev.theolm.record.Record
import dev.theolm.record.config.AudioEncoder
import dev.theolm.record.config.OutputFormat
import dev.theolm.record.config.OutputLocation
import dev.theolm.record.config.RecordConfig
import io.github.aakira.napier.Napier

class AudioRecorder {
    var location: String? = null

    init {
        Record.setConfig(
            RecordConfig(
                outputLocation = OutputLocation.Cache,
                outputFormat = OutputFormat.WAV,
                audioEncoder = AudioEncoder.PCM_16BIT,
                sampleRate = 24_000,
            )
        )
    }

    fun record() {
        Napier.d(tag = TAG) { "Recording..." }
        Record.startRecording()
    }

    fun stopRecording() {
        Record.stopRecording().also { savedAudioPath: String ->
            location = savedAudioPath.split("/").last()
            Napier.d(tag = TAG) { "Recording saved to: $savedAudioPath" }
        }
    }

    fun isRecording() = Record.isRecording()
}

private const val TAG = "AudioRecorder"