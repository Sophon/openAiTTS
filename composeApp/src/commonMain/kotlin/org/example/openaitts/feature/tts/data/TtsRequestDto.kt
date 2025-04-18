package org.example.openaitts.feature.tts.data

import kotlinx.serialization.Serializable

@Serializable
data class TtsRequestDto(
    val model: String,
    val input: String,
    val voice: String,
)
