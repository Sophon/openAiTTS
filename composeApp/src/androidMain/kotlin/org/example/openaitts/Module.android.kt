package org.example.openaitts

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import org.example.openaitts.feature.tts.domain.AudioFileManager
import org.koin.dsl.module

actual val platformModule = module {
    //core
    single<HttpClientEngine> { Android.create() }

    //tts
    single { AudioFileManager(get()) }
}