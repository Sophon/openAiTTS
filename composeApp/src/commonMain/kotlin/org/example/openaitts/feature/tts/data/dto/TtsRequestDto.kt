package org.example.openaitts.feature.tts.data.dto

data class TtsRequestDto(
    val model: String,
    val input: String,
    val voice: String,
)
