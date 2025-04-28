package org.example.openaitts.feature.conversation.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Voice {
        @SerialName("alloy") ALLOY,
        @SerialName("ash") ASH,
        @SerialName("ballad") BALLAD,
        @SerialName("coral") CORAL,
        @SerialName("echo") ECHO,
        @SerialName("sage") SAGE,
        @SerialName("shimmer") SHIMMER,
        @SerialName("verse") VERSE,
}