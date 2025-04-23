package org.example.openaitts.feature.conversation.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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