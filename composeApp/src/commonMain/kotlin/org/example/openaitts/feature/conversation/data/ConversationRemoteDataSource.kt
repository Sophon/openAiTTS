package org.example.openaitts.feature.conversation.data

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import org.example.openaitts.core.data.BASE_URL
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.core.network.safeCall
import org.example.openaitts.feature.conversation.data.dto.SessionResponseDto

class ConversationRemoteDataSource(
    private val httpClient: HttpClient,
) {
    suspend fun createSession(): Result<SessionResponseDto, DataError.Remote> {
        return safeCall { httpClient.post(SESSION_URL) }
    }

    //TODO: send message
}

private const val REALTIME_URL = "$BASE_URL/realtime"
private const val SESSION_URL = "$REALTIME_URL/sessions"