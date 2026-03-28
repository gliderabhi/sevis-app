package com.sevis.app.presentation.util

import androidx.compose.runtime.Composable

@Composable
expect fun FilePicker(
    show: Boolean,
    onFilePicked: (name: String, bytes: ByteArray) -> Unit,
    onDismiss: () -> Unit
)
