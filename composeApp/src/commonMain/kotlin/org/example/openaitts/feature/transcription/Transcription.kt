package org.example.openaitts.feature.transcription

import kotlinx.serialization.Serializable

@Serializable
data class Transcription(
    val text: String,
)
