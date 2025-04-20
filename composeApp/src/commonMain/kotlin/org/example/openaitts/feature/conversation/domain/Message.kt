package org.example.openaitts.feature.conversation.domain

data class Message(
    val type: Type,
    val role: Role,
    val content: List<Content>,
) {
    enum class Type {
        MESSAGE,
        FUNCTION_CALL,
        FUNCTION_CALL_OUTPUT,
    }

    enum class Role {
        USER,
        ASSISTANT,
        SYSTEM,
    }

    data class Content(
        val type: Type,
        val text: String,
    ) {
        enum class Type {
            INPUT_TEXT,
            INPUT_AUDIO,
            ITEM_REFERENCE,
            TEXT,
        }
    }
}
