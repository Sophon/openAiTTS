package org.example.openaitts.feature.realtimeAgent.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAiEvent(
    val type: String,
    val delta: String? = null,
    val error: Error? = null,
    val transcript: String? = null,
    val name: String? = null,
    @SerialName("call_id")
    val callId: String? = null,
    val arguments: String? = null, //TODO: convert in actual
) {
    @Serializable
    data class Error(
        val type: String? = null,
        val code: String? = null,
        val message: String? = null
    ) {
        fun describe() = message ?: code ?: type
    }
}