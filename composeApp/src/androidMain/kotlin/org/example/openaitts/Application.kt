package org.example.openaitts

import android.app.Application
import android.app.LocaleManager
import android.content.Context
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.example.openaitts.core.PlatformContext
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent

class Application: Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger()
            androidContext(this@Application)
        }

        Napier.base(DebugAntilog())

        PlatformContext.setUp(this)
    }

//    override fun attachBaseContext(base: Context) {
//        super.attachBaseContext(
//            LocaleManager.getLocalizedContext(base)
//        )
//    }
}