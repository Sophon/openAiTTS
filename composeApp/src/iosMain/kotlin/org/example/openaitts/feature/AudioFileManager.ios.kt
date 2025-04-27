package org.example.openaitts.feature

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile

actual class AudioFileManager {
    private var player: AVAudioPlayer? = null
    private var path: String = ""

    actual fun save(data: ByteArray) {
        val dirs = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        val documentsDir = dirs.first() as String
        path = "$documentsDir/$FILENAME"
        val nsData = data.toNSData()
        nsData.writeToFile(path, true)
    }

    actual fun cache(data: ByteArray) {
        //TODO
    }

    actual fun saveCached() {
        //TODO:
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
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(bytes = allocArrayOf(this@toNSData),
        length = this@toNSData.size.toULong())
}

private const val FILENAME = "tts.mp3"