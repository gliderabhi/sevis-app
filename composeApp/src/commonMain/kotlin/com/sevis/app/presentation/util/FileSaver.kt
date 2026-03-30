package com.sevis.app.presentation.util

import androidx.compose.runtime.Composable

@Composable
expect fun FileSaver(
    bytes: ByteArray?,
    fileName: String,
    onSaved: (String) -> Unit,
    onError: (String) -> Unit
)