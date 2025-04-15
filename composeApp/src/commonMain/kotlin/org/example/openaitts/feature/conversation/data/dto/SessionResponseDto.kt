package org.example.openaitts.feature.conversation.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionResponseDto(
    val id: String,
    @SerialName("object") val objectType: String,
    val model: String,
    val modalities: List<String>,
    val instructions: String,
    val voice: String,
    @SerialName("input_audio_format") val inputAudioFormat: String,
    @SerialName("output_audio_format") val outputAudioFormat: String,
    @SerialName("input_audio_transcription") val inputAudioTranscription: InputAudioTranscription,
    @SerialName("turn_detection") val turnDetection: TurnDetection?, // Replace `Any?` if a structure is known
    val tools: List<Tool>,
    @SerialName("tool_choice") val toolChoice: String,
    val temperature: Double,
    @SerialName("max_response_output_tokens") val maxResponseOutputTokens: Int,
    @SerialName("client_secret") val clientSecret: ClientSecret,
) {
    @Serializable
    data class InputAudioTranscription(
        val model: String
    )

    @Serializable
    data class TurnDetection(
        @SerialName("prefix_padding_ms") val prefixPaddingMs: Int,
        @SerialName("silence_duration_ms") val silenceDurationMs: Int,
        val threshold: Double,
        val type: String,
    )

    @Serializable
    data class Tool(
        val description: String,
        val name: String,
//        val parameters: ToolParameters,
        val type: String,
    )

    @Serializable
    data class ClientSecret(
        val value: String,
        @SerialName("expires_at") val expiresAt: Long
    )
}

