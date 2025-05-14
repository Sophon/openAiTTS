package org.example.openaitts

import io.ktor.client.HttpClient
import org.example.openaitts.core.network.HttpClientFactory
import org.example.openaitts.feature.audio.AudioRecorder
import org.example.openaitts.feature.conversation.data.RealtimeWebRtcDataSource
import org.example.openaitts.feature.conversation.data.RealtimeWebSocketDataSource
import org.example.openaitts.feature.conversation.domain.usecases.AudioPlaybackUseCase
import org.example.openaitts.feature.conversation.domain.usecases.ConversationUseCase
import org.example.openaitts.feature.conversation.domain.usecases.RecordAudioUseCase
import org.example.openaitts.feature.conversation.domain.usecases.SendConversationMessageUseCase
import org.example.openaitts.feature.conversation.domain.usecases.StopAudioRecordingUseCase
import org.example.openaitts.feature.conversation.domain.usecases.UpdateVoiceUseCase
import org.example.openaitts.feature.conversation.ui.ConversationViewModel
import org.example.openaitts.feature.transcription.TranscribeAudioMessageUseCase
import org.example.openaitts.feature.transcription.TranscriptionRemoteDataSource
import org.example.openaitts.feature.transcription.TranscriptionRemoteDataSourceImpl
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

    //region Conversation
    viewModel {
        ConversationViewModel(get(), get(), get(), get(), get(), get(), get())
    }
    singleOf(::RealtimeWebSocketDataSource)
    singleOf(::RealtimeWebRtcDataSource)
    singleOf(::ConversationUseCase)
    singleOf(::SendConversationMessageUseCase)
    singleOf(::UpdateVoiceUseCase)
    singleOf(::RecordAudioUseCase)
    singleOf(::StopAudioRecordingUseCase)
    //endregion

    //region Audio
    singleOf(::AudioPlaybackUseCase)
    singleOf(::AudioRecorder)
    //endregion

    //region Transcription
    singleOf(::TranscriptionRemoteDataSourceImpl).bind<TranscriptionRemoteDataSource>()
    singleOf(::TranscribeAudioMessageUseCase)
    //endregion
}