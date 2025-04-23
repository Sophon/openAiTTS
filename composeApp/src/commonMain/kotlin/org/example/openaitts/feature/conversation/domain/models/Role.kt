package org.example.openaitts.feature.conversation.domain.models

import kotlinx.serialization.SerialName

enum class Role {
    @SerialName("user") USER,
    @SerialName("assistant") ASSISTANT,
    @SerialName("system") SYSTEM,
}