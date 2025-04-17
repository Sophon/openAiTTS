package org.example.openaitts.feature.conversation.data.dto

data class MessageDto(
    val type: String = "user_message",
    val text: String,
)
