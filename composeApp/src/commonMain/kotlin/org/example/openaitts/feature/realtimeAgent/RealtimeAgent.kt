package org.example.openaitts.feature.realtimeAgent

import org.example.openaitts.core.PlatformContext
import org.example.openaitts.feature.conversation.domain.models.Voice

expect class RealtimeAgent(
    callbacks: RealtimeAgentCallbacks,
) {
    val platformContext: PlatformContext

    fun start(apiKey: String, voice: Voice)
    fun stop()
    fun toggleMic(newValue: Boolean)
}

interface RealtimeAgentCallbacks {
    fun onConnect()
    fun onDisconnect()
    fun onBackendError(message: String)
    fun onAgentReady()

    fun onAgentTranscriptionDeltaReceived(transcript: String)
    fun onAgentTranscriptionDone()
    fun onUserTranscriptionReceived(text: String, isFinal: Boolean)

    fun onAgentTalking()
    fun onAgentTalkingDone()

    fun onUserTalking()
    fun onUserTalkingDone()
}