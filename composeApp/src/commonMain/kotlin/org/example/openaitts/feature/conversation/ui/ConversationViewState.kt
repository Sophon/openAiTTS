package org.example.openaitts.feature.conversation.ui

import org.example.openaitts.feature.conversation.domain.Session

data class ConversationViewState(
    val error: String? = null,
    val isLoading: Boolean = false,

    val session: Session? = null,
)