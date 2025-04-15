package org.example.openaitts.feature.tts.domain

expect object AudioPlayer {
    fun play(path: String)
    fun stop()
}