package org.example.openaitts.feature.conversation.data.dto

data class CreateResponseRequestDto(
    val type: String = "response.create",
    val response: Response,
) {
    data class Response(
        val modalities: List<Modality>,
    ) {
        enum class Modality(val value: String) {
            AUDIO("audio"),
            TEXT("text"),
        }
    }
}