package com.sevis.app.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun FileSaver(
    bytes: ByteArray?,
    fileName: String,
    onSaved: (String) -> Unit,
    onError: (String) -> Unit
) {
    // iOS file saving not yet implemented
    LaunchedEffect(bytes) {
        if (bytes != null) onError("PDF download not supported on iOS yet")
    }
}