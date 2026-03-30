package com.sevis.app.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
actual fun FileSaver(
    bytes: ByteArray?,
    fileName: String,
    onSaved: (String) -> Unit,
    onError: (String) -> Unit
) {
    LaunchedEffect(bytes) {
        if (bytes == null) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            try {
                val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Save PDF", java.awt.FileDialog.SAVE)
                dialog.file = fileName
                dialog.isVisible = true
                val dir  = dialog.directory
                val file = dialog.file
                if (dir != null && file != null) {
                    File(dir, file).writeBytes(bytes)
                    withContext(Dispatchers.Main) { onSaved(file) }
                } else {
                    withContext(Dispatchers.Main) { onError("Cancelled") }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e.message ?: "Save failed") }
            }
        }
    }
}