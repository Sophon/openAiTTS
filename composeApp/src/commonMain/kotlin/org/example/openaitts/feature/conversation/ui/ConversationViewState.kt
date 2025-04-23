package org.example.openaitts.feature.conversation.ui

import org.example.openaitts.feature.conversation.domain.models.Content
import org.example.openaitts.feature.conversation.domain.models.MessageItem
import org.example.openaitts.feature.conversation.domain.models.MessageItem.Type
import org.example.openaitts.feature.conversation.domain.models.Role


data class ConversationViewState(
    val error: String? = null,
    val isLoading: Boolean = false,

    val messages: List<UiMessage> = listOf(),
)

data class UiMessage(
    val type: Type,
    val role: Role,
    val content: Content,
)

internal fun MessageItem.toUi(): UiMessage {
    return UiMessage(
        type = this.type,
        role = this.role,
        content = this.content.first(),
    )
}