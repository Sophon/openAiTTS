package org.example.openaitts.feature.conversation.data.dto

import kotlinx.serialization.SerialName
import org.example.openaitts.feature.conversation.domain.models.EventType

data class RequestRetrieveItemDto(
    val type: EventType,
    @SerialName("item_id") val itemId: String,
)