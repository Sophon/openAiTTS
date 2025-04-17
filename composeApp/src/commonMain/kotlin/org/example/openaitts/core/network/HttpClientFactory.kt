package org.example.openaitts.core.network

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.openaitts.BuildKonfig.API_KEY

object HttpClientFactory {
    fun create(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            install(
                plugin = ContentNegotiation,
                configure = {
                    json(json = Json(builderAction = { ignoreUnknownKeys = true }))
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
                        Napier.v(message = message, tag = TAG)
                    }
                }
                level = LogLevel.HEADERS
            }

            install(WebSockets)

            defaultRequest {
                headers { append("Authorization", "Bearer $API_KEY") }
                contentType(ContentType.Application.Json)
            }
        }
    }
}

private const val TIMEOUT_IN_MILIS = 20_000L
private const val TAG = "Ktor"