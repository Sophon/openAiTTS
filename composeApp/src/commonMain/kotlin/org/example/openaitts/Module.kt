package org.example.openaitts

import io.ktor.client.HttpClient
import org.example.openaitts.core.network.HttpClientFactory
import org.example.openaitts.feature.chat.ChatViewModel
import org.example.openaitts.feature.chat.data.remote.ChatRemoteDataSource
import org.example.openaitts.feature.chat.data.remote.ChatRemoteDataSourceImpl
import org.example.openaitts.feature.chat.domain.SendMessageUseCase
import org.example.openaitts.feature.tts.data.TtsRemoteDataSource
import org.example.openaitts.feature.tts.data.TtsRemoteDataSourceImpl
import org.example.openaitts.feature.tts.domain.AudioFileManager
import org.example.openaitts.feature.tts.domain.PromptTTSUseCase
import org.example.openaitts.feature.tts.ui.TtsViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {
    return startKoin {
        config?.invoke(this)

        modules(
            sharedModule,
            platformModule,
        )
    }
}

expect val platformModule: Module

val sharedModule = module {
    //region Core
    single<HttpClient> { HttpClientFactory.create(get()) }
    //endregion

    //region Chat
    viewModel { ChatViewModel(get()) }
    singleOf(::SendMessageUseCase)
    singleOf(::ChatRemoteDataSourceImpl).bind<ChatRemoteDataSource>()
    //endregion

    //region TTS
    viewModel { TtsViewModel(get(), get()) }
    singleOf(::TtsRemoteDataSourceImpl).bind<TtsRemoteDataSource>()
    singleOf(::AudioFileManager)
    singleOf(::PromptTTSUseCase)
    //endregion
}