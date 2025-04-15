package org.example.openaitts.feature.conversation.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionRequestDto(
    val model: String,
)