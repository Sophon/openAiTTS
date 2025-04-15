package org.example.openaitts.feature.conversation.domain

import io.github.aakira.napier.Napier
import org.example.openaitts.feature.conversation.data.dto.SessionResponseDto

internal fun SessionResponseDto.toDomain(): Session {
    return Session(
        id = this.id,
        model = this.model,
        instructions = this.instructions,
        voice = this.voice.toDomainVoice(),
        inputAudioFormat = this.inputAudioFormat.toDomainAudioFormat(),
        outputAudioFormat = this.outputAudioFormat.toDomainAudioFormat(),
        turnDetection = this.turnDetection?.toDomain(),
        temperature = this.temperature,
        maxResponseOutputTokens = this.maxResponseOutputTokens.toDomainMaxResponseOutput(),
        clientSecret = this.clientSecret.toDomain(),
    )
}

internal fun String.toDomainVoice(): Session.Voice {
    return try {
        Session.Voice.valueOf(this.uppercase())
    } catch (e: IllegalArgumentException) {
        Napier.e(message = "Voice: ${e.message}", tag = TAG)
        Session.Voice.ASH
    }
}

internal fun String.toDomainAudioFormat(): Session.AudioFormat {
    return try {
        Session.AudioFormat.valueOf(this.uppercase())
    } catch (e: IllegalArgumentException) {
        Napier.e(message = "AudioFormat: ${e.message}", tag = TAG)
        Session.AudioFormat.PCM16
    }
}

internal fun SessionResponseDto.TurnDetection.toDomain(): Session.TurnDetection {
    return Session.TurnDetection(
        prefixPaddingMs = this.prefixPaddingMs,
        silenceDurationMs = this.silenceDurationMs,
        threshold = this.threshold,
        type = this.type,
    )
}

internal fun SessionResponseDto.ClientSecret.toDomain(): Session.ClientSecret {
    return Session.ClientSecret(
        value = this.value,
        expiresAt = this.expiresAt,
    )
}

internal fun String.toDomainMaxResponseOutput(): Int {
    return when (this) {
        "inf" -> Int.MAX_VALUE
        else -> {
            try {
                this.toInt()
            } catch (e: NumberFormatException) {
                Napier.e(message = "MaxResponseOutput: ${e.message}", tag = TAG)
                Int.MAX_VALUE
            }
        }
    }
}

private const val TAG = "ConversationMappers"