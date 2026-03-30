package com.sevis.app.presentation.util

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun FilePicker(
    show: Boolean,
    onFilePicked: (name: String, bytes: ByteArray) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            val name = uri.lastPathSegment ?: "parts.csv"
            if (bytes != null) onFilePicked(name, bytes) else onDismiss()
        } else {
            onDismiss()
        }
    }
    LaunchedEffect(show) {
        if (show) launcher.launch(arrayOf(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/csv",
            "text/tab-separated-values"
        ))
    }
}
