package org.example.openaitts.feature.conversation.data.dto

data class MessageDto(
    val type: String = "conversation.item.create",
    val item: Item,
) {
    data class Item(
        val type: Type,
        val role: Role,
        val content: Content,
    ) {
        enum class Type(val value: String) {
            MESSAGE("message"),
            FUNCTION_CALL("function_call"),
            FUNCTION_CALL_OUTPUT("function_call_output"),
        }

        enum class Role(val value: String) {
            USER("user"),
            ASSISTANT("assistant"),
            SYSTEM("system"),
        }

        data class Content(
            val type: Type,
            val text: String,
        ) {
            enum class Type(val value: String) {
                INPUT_TEXT("input_text"),
                INPUT_AUDIO("input_audio"),
                ITEM_REFERENCE("item_reference"),
                TEXT("text"),
            }
        }
    }
}
