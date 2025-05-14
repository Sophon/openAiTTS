package org.example.openaitts.feature.conversation.domain.usecases

import org.example.openaitts.feature.conversation.data.RealtimeWebSocketDataSource
import org.example.openaitts.feature.conversation.data.dto.RequestUpdateSessionDto
import org.example.openaitts.feature.conversation.domain.models.EventType
import org.example.openaitts.feature.conversation.domain.models.Session
import org.example.openaitts.feature.conversation.domain.models.Voice

class UpdateVoiceUseCase(
    private val remoteDataSource: RealtimeWebSocketDataSource,
) {
    suspend fun updateVoice(voice: Voice) {
        remoteDataSource.updateSession(
            RequestUpdateSessionDto(
                type = EventType.SESSION_UPDATE,
                session = Session(voice = voice)
            )
        )
    }
}