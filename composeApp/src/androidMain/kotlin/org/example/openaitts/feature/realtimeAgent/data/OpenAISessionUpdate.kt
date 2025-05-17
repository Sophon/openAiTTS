package org.example.openaitts.feature.realtimeAgent.data

import ai.pipecat.client.types.Value
import kotlinx.serialization.Serializable

@Serializable
internal data class OpenAISessionUpdate private constructor(
    val type: String,
    val session: Value
) {
    companion object {
        fun of(session: Value) = OpenAISessionUpdate(
            type = "session.update",
            session = session
        )
    }
}
