package org.example.openaitts.core

import android.content.Context
import org.example.openaitts.Application

actual object PlatformContext {
    private lateinit var application: Application

    fun setUp(context: Context) {
        application = context as Application
    }

    fun get(): Context {
        if (::application.isInitialized.not()) {
            throw Exception("Application context isn't initialized")
        }
        return application.applicationContext
    }
}