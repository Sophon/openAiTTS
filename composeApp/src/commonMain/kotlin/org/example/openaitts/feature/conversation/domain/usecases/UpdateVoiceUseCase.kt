package org.example.openaitts.feature.conversation.domain.usecases

import org.example.openaitts.feature.conversation.data.RealtimeRemoteDataSource
import org.example.openaitts.feature.conversation.data.dto.RequestUpdateSessionDto
import org.example.openaitts.feature.conversation.domain.models.Session
import org.example.openaitts.feature.conversation.domain.models.Voice

class UpdateVoiceUseCase(
    private val remoteDataSource: RealtimeRemoteDataSource,
) {
    suspend fun updateVoice(voice: Voice) {
        remoteDataSource.updateSession(
            RequestUpdateSessionDto(
                session = Session(voice = voice)
            )
        )
    }
}