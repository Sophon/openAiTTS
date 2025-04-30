package org.example.openaitts.feature.audio

import dev.theolm.record.Record
import dev.theolm.record.config.AudioEncoder
import dev.theolm.record.config.OutputFormat
import dev.theolm.record.config.OutputLocation
import dev.theolm.record.config.RecordConfig
import io.github.aakira.napier.Napier
import org.example.openaitts.feature.TAG

class AudioRecorder {
    private val record = Record

    init {
        Record.setConfig(
            RecordConfig(
                outputLocation = OutputLocation.Cache,
                outputFormat = OutputFormat.MPEG_4,
                audioEncoder = AudioEncoder.AAC,
            )
        )
    }

    fun record() = Record.startRecording()

    fun stopRecording() {
        Record.stopRecording().also { savedAudioPath: String ->
            Napier.d(tag = TAG) { "Recording saved to: $savedAudioPath" }
        }
    }

    fun isRecording() = Record.isRecording()
}