package org.example.openaitts.feature.conversation.domain.models

data class Session(
    val id: String,
    val model: String,
    val instructions: String,
    val voice: Voice,
    val inputAudioFormat: AudioFormat,
    val outputAudioFormat: AudioFormat,
    val turnDetection: TurnDetection?,
    val temperature: Double,
    val maxResponseOutputTokens: Int,
    val clientSecret: ClientSecret,
) {

    enum class AudioFormat {
        PCM16,
        G711_ULAW,
        G711_ALAW,
        UNKNOWN,
    }

    data class TurnDetection(
        val prefixPaddingMs: Int,
        val silenceDurationMs: Int,
        val threshold: Double,
        val type: String,
    )

    data class InputAudioTranscription(
        val model: String
    )

    data class ClientSecret(
        val value: String,
        val expiresAt: Long
    )
}
