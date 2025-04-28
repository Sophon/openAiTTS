package org.example.openaitts.feature.conversation.ui

import org.example.openaitts.feature.conversation.domain.models.Content
import org.example.openaitts.feature.conversation.domain.models.MessageItem
import org.example.openaitts.feature.conversation.domain.models.MessageItem.Type
import org.example.openaitts.feature.conversation.domain.models.Role
import org.example.openaitts.feature.conversation.domain.models.Voice

data class ConversationViewState(
    val error: String? = null,
    val isLoading: Boolean = false,

    val query: String = "",
    val messages: List<UiMessage> = listOf(),
    val selectedVoice: Voice = Voice.ALLOY,
    val isVoiceSelectorDialogVisible: Boolean = false,
) {
    val isSendEnabled: Boolean get() = query.isNotBlank()
    val selectedVoiceInitials: String get() = selectedVoice.name.take(3)
}

data class UiMessage(
    val type: Type,
    val role: Role,
    val text: String?,
)

internal fun MessageItem.toUi(): UiMessage {
    return UiMessage(
        type = this.type,
        role = this.role,
        text = when (this.content.last().type) {
            Content.Type.TEXT -> this.content.last().text
            Content.Type.AUDIO -> this.content.last().transcript
            else -> null
        },
    )
}