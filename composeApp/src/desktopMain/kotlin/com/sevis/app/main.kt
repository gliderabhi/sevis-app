package com.sevis.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.sevis.app.di.appModules
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

fun main() = application {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            modules(appModules)
        }
    }

    val viewModelStoreOwner = object : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Sevis App",
        state = rememberWindowState(size = DpSize(420.dp, 915.dp)),
        resizable = false
    ) {
        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            App()
        }
    }
}
