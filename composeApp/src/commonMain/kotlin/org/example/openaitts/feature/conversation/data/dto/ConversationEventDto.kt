package org.example.openaitts.feature.conversation.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationEventDto(
    val type: Type,
    val session: Session? = null,
    val response: Item? = null,
    val item: Item? = null,
    @SerialName("event_id") val eventId: String? = null,
    val error: Error? = null,
) {
    enum class Type {
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
        @SerialName("response.content_part.done") RESPONSE_CONTENT_PART_DONE,
        @SerialName("response.output_item.done") RESPONSE_OUTPUT_ITEM_DONE,
        @SerialName("response.done") RESPONSE_DONE,
        @SerialName("rate_limits.updated") RATE_LIMITS_UPDATED,

        @SerialName("error") ERROR,
    }

    @Serializable
    data class Session(
        val id: String,
        @SerialName("object") val objectType: String,
        val model: String,
        val modalities: List<String>,
        val instructions: String,
        val voice: String,
        @SerialName("input_audio_format") val inputAudioFormat: String,
        @SerialName("output_audio_format") val outputAudioFormat: String,
        @SerialName("input_audio_transcription") val inputAudioTranscription: InputAudioTranscription?,
        @SerialName("turn_detection") val turnDetection: TurnDetection?, // Replace `Any?` if a structure is known
        val tools: List<Tool>,
        @SerialName("tool_choice") val toolChoice: String,
        val temperature: Double,
        @SerialName("max_response_output_tokens") val maxResponseOutputTokens: String,
        @SerialName("client_secret") val clientSecret: ClientSecret? = null,
    ) {
        @Serializable
        data class InputAudioTranscription(
            val model: String
        )

        @Serializable
        data class TurnDetection(
            @SerialName("prefix_padding_ms") val prefixPaddingMs: Int,
            @SerialName("silence_duration_ms") val silenceDurationMs: Int,
            val threshold: Double,
            val type: String,
        )

        @Serializable
        data class Tool(
            val description: String,
            val name: String,
//        val parameters: ToolParameters,
            val type: String,
        )

        @Serializable
        data class ClientSecret(
            val value: String,
            @SerialName("expires_at") val expiresAt: Long
        )
    }

    @Serializable
    data class Item(
        val id: String? = null,
        @SerialName("object") val responseObject: String? = null, //TODO
        val type: Type? = null,
        val status: String? = null, //TODO
        val role: Role? = null,
        val content: List<Content>? = null,
        val modalities: List<Modality>? = null,
    ) {
        enum class Type {
            @SerialName("message") MESSAGE,
            @SerialName("function_call") FUNCTION_CALL,
            @SerialName("function_call_output")FUNCTION_CALL_OUTPUT,
        }

        enum class Role {
            @SerialName("user") USER,
            @SerialName("assistant") ASSISTANT,
            @SerialName("system") SYSTEM,
        }

        @Serializable
        data class Content(
            val type: Type,
            val text: String,
        ) {
            enum class Type {
                @SerialName("input_text") INPUT_TEXT,
                @SerialName("input_audio") INPUT_AUDIO,
                @SerialName("item_reference") ITEM_REFERENCE,
                @SerialName("text") TEXT,
            }
        }

        enum class Modality {
            @SerialName("audio") AUDIO,
            @SerialName("text") TEXT,
        }
    }

    @Serializable
    data class Error(
        val type: String,
        val code: String,
        val message: String,
    )
}