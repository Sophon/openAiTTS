package org.example.openaitts.feature.realtimeAgent

import org.example.openaitts.core.PlatformContext

expect class RealtimeAgent(
    callbacks: RealtimeAgentCallbacks,
) {
    val platformContext: PlatformContext

    fun start(apiKey: String)
    fun stop()
    fun toggleMic(newValue: Boolean)
}

interface RealtimeAgentCallbacks {
    fun onConnect()
    fun onDisconnect()
    fun onBackendError(message: String)
    fun onAgentReady()

    fun onAgentTranscriptionReceived(transcript: String)
    fun onUserTranscriptionReceived(text: String, isFinal: Boolean)

    fun onAgentTalking()
    fun onAgentTalkingDone()

    fun onUserTalking()
    fun onUserTalkingDone()
}