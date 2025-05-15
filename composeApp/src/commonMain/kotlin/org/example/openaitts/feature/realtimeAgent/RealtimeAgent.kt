package org.example.openaitts.feature.realtimeAgent

import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import org.example.openaitts.core.PlatformContext

expect class RealtimeAgent(
    callbacks: RealtimeAgentCallbacks,
    apiKey: String,
) {
    val platformContext: PlatformContext

    val isBotReady: MutableState<Boolean>
    val isBotTalking: MutableState<Boolean>
    val isUserTalking: MutableState<Boolean>
    val botAudioLevel: MutableFloatState
    val userAudioLevel: MutableFloatState
    val isMicEnabled: MutableState<Boolean>

    fun start()
    fun stop()
    fun toggleMic(newValue: Boolean)
}

interface RealtimeAgentCallbacks {
    fun onConnect()
    fun onDisconnect()
    fun onBackendError(message: String)
    fun onBotReady()

    fun onBotTranscriptionReceived(transcript: String)
    fun onUserTranscriptionReceived(text: String, isFinal: Boolean)

    fun onBotStartedSpeaking()
    fun onBotStoppedSpeaking()

    fun onUserStartedSpeaking()
    fun onUserStoppedSpeaking()
}