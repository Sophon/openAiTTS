package org.example.openaitts.feature.conversation.data

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.withCharset
import io.ktor.serialization.ContentConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.readText
import io.ktor.utils.io.readRemaining
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.example.openaitts.core.data.REALTIME_URL
import org.example.openaitts.core.domain.DataError
import org.example.openaitts.core.domain.Result
import org.example.openaitts.core.network.safeCall
import org.example.openaitts.core.network.toResult
import org.example.openaitts.feature.conversation.data.dto.RequestSessionDto
import org.example.openaitts.feature.conversation.domain.models.Session

class RealtimeWebRtcDataSource(
    private val httpClient: HttpClient,
    clientEngine: HttpClientEngine,
) {
    private val sdpClient: HttpClient = createHttpClient(clientEngine)

    suspend fun getSession(request: RequestSessionDto): Result<Session, DataError.Remote> {
        return safeCall { httpClient.post("$REALTIME_URL/sessions") { setBody(request) } }
    }

    suspend fun postSdp(
        ephemeralKey: String,
        sdp: String,
    ): Result<String, DataError.Remote> {
        val response: HttpResponse
        try {
            response = sdpClient.post(REALTIME_URL) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $ephemeralKey")
                    append(HttpHeaders.ContentType, "application/sdp")
                }
                setBody(sdp)
            }
            return response.toResult()
        } catch (e: Exception) {
            Napier.d(tag = "Ktor") { "Error! ${e.message}" }
            return Result.Error(DataError.Remote.UNKNOWN)
        }
    }

    /**
     * Because our default http client has default content as Json, we need to create this one for SDP
     */
    private fun createHttpClient(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            install(
                plugin = ContentNegotiation,
                configure = {
                    json(json = Json(builderAction = { ignoreUnknownKeys = true }))
                    register(ContentType.parse("application/sdp"), SdpContentConverter)
                }
            )

            install(
                plugin = HttpTimeout,
                configure = {
                    socketTimeoutMillis = TIMEOUT_IN_MILIS
                    requestTimeoutMillis = TIMEOUT_IN_MILIS
                }
            )

            install(HttpCache)

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.v(message = message, tag = TAG_KTOR)
                    }
                }
                level = LogLevel.ALL
            }

            install(WebSockets)
        }
    }
}

private object SdpContentConverter : ContentConverter {
    override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: ByteReadChannel
    ): Any {
        val text = content.readRemaining().readText(charset)
        return text
    }

    override suspend fun serialize(contentType: ContentType, charset: Charset, typeInfo: TypeInfo, value: Any?): OutgoingContent? {
        val text = value as? String
            ?: throw SerializationException("Expected String for SDP body")

        return TextContent(text, contentType.withCharset(charset))
    }
}

private const val TAG = "RealtimeWebRtcDataSource"
private const val TAG_KTOR = "Ktor"
private const val TIMEOUT_IN_MILIS = 20_000L