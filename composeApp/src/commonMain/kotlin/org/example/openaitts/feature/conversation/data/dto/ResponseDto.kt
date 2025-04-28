package org.example.openaitts.feature.conversation.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.openaitts.feature.conversation.domain.models.EventType
import org.example.openaitts.feature.conversation.domain.models.MessageItem
import org.example.openaitts.feature.conversation.domain.models.Session

@Serializable
data class ResponseDto(
    val type: EventType,
    @SerialName("event_id") val eventId: String,
    val item: MessageItem? = null,
    val delta: String? = null,
    val error: Error? = null,
    val session: Session? = null,
) {

    @Serializable
    data class Error(
        val type: String,
        val code: String,
        val message: String,
    )
}
