package org.example.openaitts.feature

expect class AudioFileManager {
    fun save(data: ByteArray)
    fun cache(data: ByteArray)
    fun saveCached()
    fun play()
    fun stop()
}