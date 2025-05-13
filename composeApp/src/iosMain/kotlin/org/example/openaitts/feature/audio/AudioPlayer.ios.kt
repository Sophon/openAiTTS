package org.example.openaitts.feature.audio

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSURL

actual class AudioPlayer {
    private var player: AVAudioPlayer? = null
    private var path: String = ""

    actual fun cache(data: ByteArray) {
        //TODO
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun play() {
        val url = NSURL.fileURLWithPath(path)
        player = AVAudioPlayer(contentsOfURL = url, error = null)
        player?.apply {
            prepareToPlay()
            play()
        }
    }

    actual fun stop() {
        player?.stop()
        player = null
    }

    actual fun retrieveFile(path: String): ByteArray? {
        TODO()
    }
}
