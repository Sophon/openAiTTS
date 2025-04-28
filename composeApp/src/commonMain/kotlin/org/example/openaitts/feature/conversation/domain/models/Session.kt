package org.example.openaitts.feature.conversation.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: String? = null,
    val model: String? = null,
    val modalities: List<Modality> = listOf(),
    val instructions: String? = null,
    val voice: Voice? = null,
    val inputAudioFormat: AudioFormat? = null,
    val outputAudioFormat: AudioFormat? = null,
    val turnDetection: TurnDetection? = null,
    val temperature: Double? = null,
    val maxResponseOutputTokens: Int? = null,
    val clientSecret: ClientSecret? = null,
) {
    enum class AudioFormat {
        @SerialName("pcm16") PCM16,
        @SerialName("g711_ulaw") G711_ULAW,
        @SerialName("g711_alaw") G711_ALAW,
        UNKNOWN,
    }

    @Serializable
    data class TurnDetection(
        val prefixPaddingMs: Int,
        val silenceDurationMs: Int,
        val threshold: Double,
        val type: String,
    )

    data class InputAudioTranscription(
        val model: String
    )

    @Serializable
    data class ClientSecret(
        val value: String,
        val expiresAt: Long
    )
}
