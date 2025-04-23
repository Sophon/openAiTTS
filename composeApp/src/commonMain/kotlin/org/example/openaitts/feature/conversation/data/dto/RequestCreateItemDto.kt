package org.example.openaitts.feature.conversation.data.dto

import kotlinx.serialization.Serializable
import org.example.openaitts.feature.conversation.domain.models.EventType
import org.example.openaitts.feature.conversation.domain.models.MessageItem

@Serializable
data class RequestCreateItemDto(
    val type: EventType,
    val item: MessageItem,
)
