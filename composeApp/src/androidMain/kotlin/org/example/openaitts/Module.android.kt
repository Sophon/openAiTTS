package org.example.openaitts

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import org.example.openaitts.feature.audio.AudioPlayer
import org.example.openaitts.feature.realtimeAgent.RealtimeAgent
import org.example.openaitts.feature.realtimeAgent.data.WebRTCClient
import org.koin.dsl.module

actual val platformModule = module {
    //core
    single<HttpClientEngine> { CIO.create() }

    //tts
    single { AudioPlayer(get()) }

    //realtime agent
    single { RealtimeAgent(get()) }
    single { WebRTCClient(get(), get()) }
}