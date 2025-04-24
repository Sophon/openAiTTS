package org.example.openaitts.feature.conversation.domain.models

import kotlinx.serialization.SerialName

enum class EventType {
    //session
    @SerialName("session.created") SESSION_CREATED,
    @SerialName("session.update") SESSION_UPDATE,
    @SerialName("session.updated") SESSION_UPDATED,

    //client side
    @SerialName("response.create") RESPONSE_CREATE,
    @SerialName("conversation.item.create") ITEM_CREATE,

    //server side
    @SerialName("conversation.item.created") ITEM_CREATED,
    @SerialName("response.created") RESPONSE_CREATED,
    @SerialName("response.output_item.added") RESPONSE_OUTPUT_ITEM_ADDED,
    @SerialName("response.content_part.added") RESPONSE_CONTENT_PART_ADDED,
    @SerialName("response.text.delta") RESPONSE_TEXT_DELTA,
    @SerialName("response.text.done") RESPONSE_TEXT_DONE,
    @SerialName("response.audio.delta") RESPONSE_AUDIO_DELTA,
    @SerialName("response.audio.done") RESPONSE_AUDIO_DONE,
    @SerialName("response.audio_transcript.delta") RESPONSE_AUDIO_TRANSCRIPT_DELTA,
    @SerialName("response.audio_transcript.done") RESPONSE_AUDIO_TRANSCRIPT_DONE,
    @SerialName("response.content_part.done") RESPONSE_CONTENT_PART_DONE,
    @SerialName("response.output_item.done") RESPONSE_OUTPUT_ITEM_DONE,
    @SerialName("response.done") RESPONSE_DONE,
    @SerialName("rate_limits.updated") RATE_LIMITS_UPDATED,

    @SerialName("error") ERROR,
}