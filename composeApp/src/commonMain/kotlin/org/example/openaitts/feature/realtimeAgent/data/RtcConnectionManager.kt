package org.example.openaitts.feature.realtimeAgent.data

import kotlinx.serialization.KSerializer
import org.example.openaitts.core.PlatformContext
import org.example.openaitts.feature.conversation.domain.models.Voice
import org.example.openaitts.feature.realtimeAgent.domain.OpenAiEvent

//expect class WebRTCClient(
//    onIncomingEvent: (OpenAiEvent) -> Unit,
//    platformContext: PlatformContext,
//) {
//    fun start(apiKey: String, voice: Voice)
//
//    fun stop()
//
//    fun toggleMic(newValue: Boolean)
//}
