package org.example.openaitts.feature.audio

expect class AudioFileManager {
    fun save(data: ByteArray) //TODO: remove, redundant
    fun cache(data: ByteArray)
    fun saveCached()
    fun play()
    fun stop()
    fun retrieveFile(path: String): ByteArray?
    fun testPlay()
}