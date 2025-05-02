package org.example.openaitts.feature.audio

expect class AudioPlayer {
    fun cache(data: ByteArray)
    fun play()
    fun stop()
    fun retrieveFile(path: String): ByteArray?
}