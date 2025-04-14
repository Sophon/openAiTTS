package org.example.openaitts.feature.chat

import org.example.openaitts.feature.chat.domain.Message

data class ChatViewState(
    val question: String? = null,
    val response: String? = null,
    val error: String? = null,
) {
    val isButtonEnabled: Boolean get() = question.isNullOrEmpty().not()
}
