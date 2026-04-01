package com.sevis.app.presentation.util

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
    val context = LocalContext.current
    var pendingBytes by remember { mutableStateOf<ByteArray?>(null) }

    // On Android 10+ (API 29+) we use MediaStore — no permission needed.
    // On Android 9 and below we need WRITE_EXTERNAL_STORAGE at runtime.
    val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Permission granted — save the file that was pending
            val b = pendingBytes
            if (b != null) {
                saveToLegacyDownloads(b, fileName, onSaved, onError)
                pendingBytes = null
            }
        } else {
            onError("Storage permission denied — cannot save file")
            pendingBytes = null
        }
    }

    LaunchedEffect(bytes) {
        if (bytes == null) return@LaunchedEffect

        if (!needsPermission) {
            // Android 10+ — use MediaStore directly, no permission required
            withContext(Dispatchers.IO) {
                try {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { it.write(bytes) }
                        values.clear()
                        values.put(MediaStore.Downloads.IS_PENDING, 0)
                        resolver.update(uri, values, null, null)
                        withContext(Dispatchers.Main) { onSaved(fileName) }
                    } else {
                        withContext(Dispatchers.Main) { onError("Could not create file in Downloads") }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { onError(e.message ?: "Save failed") }
                }
            }
        } else {
            // Android 9 and below — check/request permission first
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                saveToLegacyDownloads(bytes, fileName, onSaved, onError)
            } else {
                pendingBytes = bytes
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }
}

private fun saveToLegacyDownloads(
    bytes: ByteArray,
    fileName: String,
    onSaved: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        dir.mkdirs()
        val file = File(dir, fileName)
        file.writeBytes(bytes)
        onSaved(fileName)
    } catch (e: Exception) {
        onError(e.message ?: "Save failed")
    }
}
