package org.example.openaitts

import io.ktor.client.HttpClient
import org.example.openaitts.core.network.HttpClientFactory
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
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
}