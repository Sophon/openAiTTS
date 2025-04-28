package org.example.openaitts.feature.conversation.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.openaitts.core.data.MODEL_REALTIME

@Serializable
data class Session(
//    val id: String,
//    val model: String = MODEL_REALTIME,
//    val modalities: List<Modality> = listOf(Modality.TEXT, Modality.AUDIO),
//    val instructions: String = "",
    val voice: Voice,
//    val inputAudioFormat: AudioFormat = AudioFormat.PCM16,
//    val outputAudioFormat: AudioFormat = AudioFormat.PCM16,
//    val turnDetection: TurnDetection?,
//    val temperature: Double,
//    val maxResponseOutputTokens: Int,
//    val clientSecret: ClientSecret,
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
