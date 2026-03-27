package com.sevis.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

fun main() = application {
    val viewModelStoreOwner = object : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Sevis App"
    ) {
        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            App()
        }
    }
}
