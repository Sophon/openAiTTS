package org.example.openaitts.feature.conversation.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class Answer(
    val type: String,
    val sdp: String,
)