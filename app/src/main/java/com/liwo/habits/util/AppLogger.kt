package com.liwo.habits.util

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {

    private const val LOG_DIR = "logs"
    private const val LOG_FILE = "app.log"
    private const val LOG_FILE_OLD = "app.log.1"
    private const val MAX_SIZE_BYTES = 512 * 1024L // 512 KB

    private val lock = Any()
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    @Volatile
    private var logFile: File? = null

    fun init(context: Context) {
        val dir = File(context.filesDir, LOG_DIR).also { it.mkdirs() }
        logFile = File(dir, LOG_FILE)
    }

    fun i(tag: String, msg: String) {
        Log.i(tag, msg)
        write("I", tag, msg, null)
    }

    fun e(tag: String, msg: String, throwable: Throwable? = null) {
        if (throwable != null) Log.e(tag, msg, throwable) else Log.e(tag, msg)
        write("E", tag, msg, throwable)
    }

    private fun write(level: String, tag: String, msg: String, throwable: Throwable?) {
        val file = logFile ?: return
        val timestamp = synchronized(formatter) { formatter.format(Date()) }
        val line = buildString {
            append("$timestamp | $level | $tag | $msg")
            if (throwable != null) {
                append("\n")
                append(throwable.stackTraceToString().trimEnd())
            }
            append("\n")
        }
        synchronized(lock) {
            rotateIfNeeded(file)
            file.appendText(line)
        }
    }

    private fun rotateIfNeeded(file: File) {
        if (file.exists() && file.length() >= MAX_SIZE_BYTES) {
            val old = File(file.parent, LOG_FILE_OLD)
            old.delete()
            file.renameTo(old)
        }
    }

    /** Returns concatenated log content (old file first, then current). */
    fun readAll(): String {
        val file = logFile ?: return ""
        val dir = file.parentFile ?: return ""
        return synchronized(lock) {
            val old = File(dir, LOG_FILE_OLD)
            buildString {
                if (old.exists()) append(old.readText())
                if (file.exists()) append(file.readText())
            }
        }
    }
}
