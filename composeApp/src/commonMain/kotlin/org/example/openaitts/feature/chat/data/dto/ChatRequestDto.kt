package org.example.openaitts.feature.chat.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequestDto(
    val model: String,
    val messages: List<Message>,
) {
    @Serializable
    data class Message(
        val role: String,
        val content: String,
    )
}