package com.sevis.app

import android.app.Application
import com.sevis.app.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SevisApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SevisApplication)
            modules(appModules)
        }
    }
}
