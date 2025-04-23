package org.example.openaitts.feature.conversation.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageItem(
    val type: Type,
    val role: Role,
    val content: List<Content>,
) {
    enum class Type {
        @SerialName("message") MESSAGE,
        @SerialName("function_call") FUNCTION_CALL,
        @SerialName("function_call_output") FUNCTION_CALL_OUTPUT,
    }
}