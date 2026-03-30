package com.sevis.app.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
actual fun FilePicker(
    show: Boolean,
    onFilePicked: (name: String, bytes: ByteArray) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(show) {
        if (!show) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Select File", java.awt.FileDialog.LOAD)
            dialog.setFilenameFilter { _, name ->
                name.endsWith(".xlsx", ignoreCase = true) || name.endsWith(".csv", ignoreCase = true) || name.endsWith(".tsv", ignoreCase = true)
            }
            dialog.isVisible = true
            val dir = dialog.directory
            val file = dialog.file
            if (dir != null && file != null) {
                val bytes = java.io.File(dir, file).readBytes()
                onFilePicked(file, bytes)
            } else {
                onDismiss()
            }
        }
    }
}
