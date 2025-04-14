package org.example.openaitts.feature.chat.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponseDto(
    val id: String,
    val model: String,
    val choices: List<Choice>,
) {
    @Serializable
    data class Choice(
        val index: Int,
        val message: Message,
        val finishReason: String? = null,
    ) {
        @Serializable
        data class Message(
            val role: String,
            val content: String
        )
    }
}
