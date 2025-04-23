package org.example.openaitts.feature.conversation.domain.models

import kotlinx.serialization.SerialName

enum class Modality {
    @SerialName("audio") AUDIO,
    @SerialName("text") TEXT,
}