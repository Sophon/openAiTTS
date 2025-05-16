package org.example.openaitts.feature.realtimeAgent

import org.example.openaitts.core.PlatformContext
import org.example.openaitts.feature.conversation.domain.models.Voice

actual class RealtimeAgent actual constructor(
    callbacks: RealtimeAgentCallbacks,
) {
    actual val platformContext = PlatformContext

    actual fun start(apiKey: String, voice: Voice) {
        TODO("not implemented")
    }

    actual fun stop() {
        TODO("not implemented")
    }

    actual fun toggleMic(newValue: Boolean) {
        TODO("not implemented")
    }
}