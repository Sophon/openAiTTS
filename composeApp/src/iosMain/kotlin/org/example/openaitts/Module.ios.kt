package org.example.openaitts

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.example.openaitts.feature.tts.domain.AudioPlayer
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    //core
    single<HttpClientEngine> { Darwin.create() }

    //tts
    single { AudioPlayer }
}