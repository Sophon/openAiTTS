package org.example.openaitts.feature.tts.domain

expect class AudioFileManager {
    fun save(data: ByteArray)
    fun play()
    fun stop()
}
