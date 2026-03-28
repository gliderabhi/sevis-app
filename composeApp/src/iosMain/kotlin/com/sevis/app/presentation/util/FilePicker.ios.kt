package com.sevis.app.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun FilePicker(
    show: Boolean,
    onFilePicked: (name: String, bytes: ByteArray) -> Unit,
    onDismiss: () -> Unit
) {
    // iOS file picking not yet implemented
    LaunchedEffect(show) { if (show) onDismiss() }
}
