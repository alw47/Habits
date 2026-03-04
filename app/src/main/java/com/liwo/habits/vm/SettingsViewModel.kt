package com.liwo.habits.vm

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.content.ContentValues
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liwo.habits.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate

sealed interface ExportState {
    data class Success(val message: String) : ExportState
    data class Error(val message: String) : ExportState
}

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val _exportState = MutableStateFlow<ExportState?>(null)
    val exportState: StateFlow<ExportState?> = _exportState

    fun exportToDownloads() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { saveToDownloads(getApplication()) }
            }
            _exportState.value = result.fold(
                onSuccess = { path -> ExportState.Success("Saved to $path") },
                onFailure = { e -> ExportState.Error(e.message ?: "Export failed") }
            )
        }
    }

    fun buildShareIntent(): Intent? {
        return runCatching {
            val ctx: Context = getApplication()
            val content = AppLogger.readAll()
            if (content.isBlank()) return null

            val cacheFile = File(ctx.cacheDir, "habits_log.txt")
            cacheFile.writeText(content)

            val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", cacheFile)

            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Habits App Log")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }.getOrElse {
            AppLogger.e("SettingsVM", "Failed to build share intent", it)
            null
        }
    }

    fun clearExportState() {
        _exportState.value = null
    }

    private fun saveToDownloads(ctx: Context): String {
        val content = AppLogger.readAll()
        val date = LocalDate.now().toString()
        val fileName = "habits_log_$date.txt"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = ctx.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Could not create file in Downloads")

            resolver.openOutputStream(uri)?.use { it.write(content.toByteArray()) }
                ?: error("Could not open output stream")

            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)

            "Downloads/$fileName"
        } else {
            @Suppress("DEPRECATION")
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            dir.mkdirs()
            File(dir, fileName).writeText(content)
            "Downloads/$fileName"
        }
    }
}
