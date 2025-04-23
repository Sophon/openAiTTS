package org.example.openaitts.feature.conversation.data.dto

import kotlinx.serialization.Serializable
import org.example.openaitts.feature.conversation.domain.models.EventType
import org.example.openaitts.feature.conversation.domain.models.Modality

@Serializable
data class RequestResponseDto(
    val type: EventType = EventType.RESPONSE_CREATE,
    val response: Response,
) {
    @Serializable
    data class Response(
        val modalities: List<Modality>,
    )
}