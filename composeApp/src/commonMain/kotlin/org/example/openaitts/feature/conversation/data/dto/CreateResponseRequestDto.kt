package org.example.openaitts.feature.conversation.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateResponseRequestDto(
    val type: String = "response.create",
    val response: Response,
) {
    @Serializable
    data class Response(
        val modalities: List<Modality>,
    ) {
        enum class Modality {
            @SerialName("audio") AUDIO,
            @SerialName("text") TEXT,
        }
    }
}