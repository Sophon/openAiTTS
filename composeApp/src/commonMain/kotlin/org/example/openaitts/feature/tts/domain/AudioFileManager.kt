package org.example.openaitts.feature.tts.domain

expect class AudioFileManager {
    fun save(data: ByteArray)
    fun cache(data: ByteArray)
    fun saveCached()
    fun play()
    fun stop()
}
