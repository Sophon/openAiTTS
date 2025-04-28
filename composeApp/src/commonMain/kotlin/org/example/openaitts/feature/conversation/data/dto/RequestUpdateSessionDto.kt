package org.example.openaitts.feature.conversation.data.dto

import kotlinx.serialization.Serializable
import org.example.openaitts.feature.conversation.domain.models.EventType
import org.example.openaitts.feature.conversation.domain.models.Session

@Serializable
data class RequestUpdateSessionDto(
    val type: EventType = EventType.SESSION_UPDATE,
    val session: Session,
)