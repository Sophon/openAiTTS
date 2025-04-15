package org.example.openaitts.feature.tts.domain

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSURL

actual object AudioPlayer {
    private var audioPlayer: AVAudioPlayer? = null

    @OptIn(ExperimentalForeignApi::class)
    actual fun play(path: String) {
        val url = NSURL.fileURLWithPath(path)
        audioPlayer = AVAudioPlayer(contentsOfURL = url, error = null)
        audioPlayer?.apply {
            prepareToPlay()
            play()
        }
    }

    actual fun stop() {
        audioPlayer?.stop()
        audioPlayer = null
    }
}