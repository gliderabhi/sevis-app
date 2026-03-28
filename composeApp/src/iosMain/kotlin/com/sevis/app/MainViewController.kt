package com.sevis.app

import androidx.compose.ui.window.ComposeUIViewController
import com.sevis.app.di.appModules
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(appModules)
    }
}

fun MainViewController() = ComposeUIViewController { App() }
