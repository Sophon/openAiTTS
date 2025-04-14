package org.example.openaitts.feature.chat.domain

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val role: String,
    val content: String,
)