package org.example.openaitts.feature.tts.domain

expect class AudioFileManager {
    fun save(data: ByteArray)
    fun cache(data: String)
    fun saveCached()
    fun play()
    fun stop()
}
