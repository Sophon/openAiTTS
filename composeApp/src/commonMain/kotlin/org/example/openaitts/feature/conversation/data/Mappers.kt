package org.example.openaitts.feature.conversation.data

import org.example.openaitts.feature.conversation.data.dto.MessageDto
import org.example.openaitts.feature.conversation.domain.Message

internal fun Message.toDto(): MessageDto {
    return MessageDto(
        item = MessageDto.Item(
            type = type.toDto(),
            role = role.toDto(),
            content = MessageDto.Item.Content(
                type = this.content.type.toDto(),
                text = content.text,
            )
        )
    )
}

//region ENUMS
internal fun Message.Type.toDto(): MessageDto.Item.Type {
    return when (this) {
        Message.Type.MESSAGE -> MessageDto.Item.Type.MESSAGE
        Message.Type.FUNCTION_CALL -> MessageDto.Item.Type.FUNCTION_CALL
        Message.Type.FUNCTION_CALL_OUTPUT -> MessageDto.Item.Type.FUNCTION_CALL_OUTPUT
    }
}

internal fun Message.Role.toDto(): MessageDto.Item.Role {
    return when (this) {
        Message.Role.USER -> MessageDto.Item.Role.USER
        Message.Role.ASSISTANT -> MessageDto.Item.Role.ASSISTANT
        Message.Role.SYSTEM -> MessageDto.Item.Role.SYSTEM
    }
}

internal fun Message.Content.Type.toDto(): MessageDto.Item.Content.Type {
    return when (this) {
        Message.Content.Type.INPUT_TEXT -> MessageDto.Item.Content.Type.INPUT_TEXT
        Message.Content.Type.INPUT_AUDIO -> MessageDto.Item.Content.Type.INPUT_AUDIO
        Message.Content.Type.ITEM_REFERENCE -> MessageDto.Item.Content.Type.ITEM_REFERENCE
        Message.Content.Type.TEXT -> MessageDto.Item.Content.Type.TEXT
    }
}
//endregion

internal fun MessageDto.toDomain(): Message {
    return Message(
        type = item.type.toDomain(),
        role = item.role.toDomain(),
        content = Message.Content(
            type = item.content.type.toDomain(),
            text = item.content.text,
        )
    )
}

//region ENUMS
internal fun MessageDto.Item.Type.toDomain(): Message.Type {
    return when (this) {
        MessageDto.Item.Type.MESSAGE -> Message.Type.MESSAGE
        MessageDto.Item.Type.FUNCTION_CALL -> Message.Type.FUNCTION_CALL
        MessageDto.Item.Type.FUNCTION_CALL_OUTPUT -> Message.Type.FUNCTION_CALL_OUTPUT
    }
}

internal fun MessageDto.Item.Role.toDomain(): Message.Role {
    return when (this) {
        MessageDto.Item.Role.USER -> Message.Role.USER
        MessageDto.Item.Role.ASSISTANT -> Message.Role.ASSISTANT
        MessageDto.Item.Role.SYSTEM -> Message.Role.SYSTEM
    }
}

internal fun MessageDto.Item.Content.Type.toDomain(): Message.Content.Type {
    return when (this) {
        MessageDto.Item.Content.Type.INPUT_TEXT -> Message.Content.Type.INPUT_TEXT
        MessageDto.Item.Content.Type.INPUT_AUDIO -> Message.Content.Type.INPUT_AUDIO
        MessageDto.Item.Content.Type.ITEM_REFERENCE -> Message.Content.Type.ITEM_REFERENCE
        MessageDto.Item.Content.Type.TEXT -> Message.Content.Type.TEXT
    }
}
//endregion