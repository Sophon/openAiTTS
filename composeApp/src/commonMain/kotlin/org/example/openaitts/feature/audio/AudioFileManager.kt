package org.example.openaitts.feature.audio

expect class AudioFileManager {
    fun cache(data: ByteArray)
    fun play()
    fun stop()
    fun retrieveFile(path: String): ByteArray?
    fun testPlay()
}