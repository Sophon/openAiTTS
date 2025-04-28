package org.example.openaitts.feature.conversation.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Modality {
    @SerialName("audio") AUDIO,
    @SerialName("text") TEXT,
}