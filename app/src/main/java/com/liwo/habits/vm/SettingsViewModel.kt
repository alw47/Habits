package com.liwo.habits.vm

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.content.ContentValues
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liwo.habits.data.repo.BackupRepository
import com.liwo.habits.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

sealed interface ExportState {
    data class Success(val message: String) : ExportState
    data class Error(val message: String) : ExportState
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportState?>(null)
    val exportState: StateFlow<ExportState?> = _exportState

    fun exportToDownloads() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { saveToDownloads(context) }
            }
            _exportState.value = result.fold(
                onSuccess = { path -> ExportState.Success("Saved to $path") },
                onFailure = { e -> ExportState.Error(e.message ?: "Export failed") }
            )
        }
    }

    fun buildShareIntent(): Intent? {
        return runCatching {
            val content = AppLogger.readAll()
            if (content.isBlank()) return null

            val cacheFile = File(context.cacheDir, "habits_log.txt")
            cacheFile.writeText(content)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheFile)

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

    fun exportBackup() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { backupRepository.exportToDownloads() }
            }
            _exportState.value = result.fold(
                onSuccess = { path -> ExportState.Success("Backup saved to $path") },
                onFailure = { e -> ExportState.Error(e.message ?: "Export failed") }
            )
        }
    }

    fun clearDatabase() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { backupRepository.clearAll() }
            }
            _exportState.value = result.fold(
                onSuccess = { ExportState.Success("All data cleared") },
                onFailure = { e -> ExportState.Error(e.message ?: "Clear failed") }
            )
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { backupRepository.importFromUri(uri) }
            }
            _exportState.value = result.fold(
                onSuccess = { ExportState.Success("Data restored successfully") },
                onFailure = { e -> ExportState.Error(e.message ?: "Import failed") }
            )
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
