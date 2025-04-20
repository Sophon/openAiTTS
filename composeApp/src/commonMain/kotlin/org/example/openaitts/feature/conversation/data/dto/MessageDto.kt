package org.example.openaitts.feature.conversation.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val type: String = "conversation.item.create",
    val item: Item,
) {
    @Serializable
    data class Item(
        val type: Type,
        val role: Role,
        val content: List<Content>,
    ) {
        enum class Type {
            @SerialName("message") MESSAGE,
            @SerialName("function_call") FUNCTION_CALL,
            @SerialName("function_call_output") FUNCTION_CALL_OUTPUT,
        }

        enum class Role {
            @SerialName("user") USER,
            @SerialName("assistant") ASSISTANT,
            @SerialName("system") SYSTEM,
        }

        @Serializable
        data class Content(
            val type: Type,
            val text: String,
        ) {
            enum class Type {
                @SerialName("input_text") INPUT_TEXT,
                @SerialName("input_audio") INPUT_AUDIO,
                @SerialName("item_reference") ITEM_REFERENCE,
                @SerialName("text") TEXT,
            }
        }
    }
}
