package org.example.openaitts.feature.realtimeAgent

import org.example.openaitts.core.PlatformContext

actual class RealtimeAgent actual constructor(
    callbacks: RealtimeAgentCallbacks,
) {
    actual val platformContext = PlatformContext

    actual fun start(apiKey: String) {
        TODO("not implemented")
    }

    actual fun stop() {
        TODO("not implemented")
    }

    actual fun toggleMic(newValue: Boolean) {
        TODO("not implemented")
    }
}