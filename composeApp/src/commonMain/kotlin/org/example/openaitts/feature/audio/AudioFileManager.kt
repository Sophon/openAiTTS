package org.example.openaitts.feature.audio

expect class AudioFileManager {
    fun save(data: ByteArray)
    fun cache(data: ByteArray)
    fun saveCached()
    fun play()
    fun stop()
}