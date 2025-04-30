package org.example.openaitts.feature.conversation.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.openaitts.feature.conversation.domain.models.EventType
import org.example.openaitts.feature.conversation.domain.models.MessageItem

@Serializable
data class RequestCreateItemDto(
    val type: EventType,
    @SerialName("event_id") val eventId: String? = null,
    val item: MessageItem,
)
