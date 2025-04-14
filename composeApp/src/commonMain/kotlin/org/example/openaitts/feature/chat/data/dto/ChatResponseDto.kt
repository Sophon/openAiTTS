package org.example.openaitts.feature.chat.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponseDto(
    val role: String,
    val content: String,
)