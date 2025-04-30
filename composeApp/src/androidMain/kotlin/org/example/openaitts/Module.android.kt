package org.example.openaitts

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import org.example.openaitts.feature.audio.AudioFileManager
import org.koin.dsl.module

actual val platformModule = module {
    //core
    single<HttpClientEngine> { CIO.create() }

    //tts
    single { AudioFileManager(get()) }
}