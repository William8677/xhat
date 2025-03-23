/*
 * Updated: 2025-02-12 23:53:08
 * Author: William8677
 */
package com.williamfq.xhat.utils.logging

import android.content.Context
import android.util.Log
import com.williamfq.xhat.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileLogger @Inject constructor(
    @ApplicationContext private val context: Context
) : LoggerInterface {

    companion object {
        private const val LOG_DIR = "logs"
        private const val MAX_LOG_AGE_DAYS = 7L
        private const val LOG_FILE_PREFIX = "app"
        private const val USER_ID = "William8677"
        private const val TIMESTAMP = "2025-02-12 23:53:08"
    }

    private val logDir: File = File(context.filesDir, LOG_DIR).apply {
        if (!exists()) mkdirs()
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    init {
        cleanOldLogs()
    }

    override suspend fun logEvent(
        tag: String,
        message: String,
        level: LogLevel,
        throwable: Throwable?
    ): Unit = withContext(Dispatchers.IO) {
        try {
            val timestamp = dateFormat.format(Date())
            val fullMessage = buildString {
                append("$timestamp [$level] $tag: $message")
                append(" [User: $USER_ID]")
                throwable?.let {
                    append("\nException: ${it.message}")
                    append("\nStack trace:\n${it.stackTraceToString()}")
                }
                append("\n")
            }

            // Debug logging
            if (BuildConfig.DEBUG) {
                when (level) {
                    LogLevel.VERBOSE -> Timber.tag(tag).v(throwable, message)
                    LogLevel.DEBUG -> Timber.tag(tag).d(throwable, message)
                    LogLevel.INFO -> Timber.tag(tag).i(throwable, message)
                    LogLevel.WARNING -> Timber.tag(tag).w(throwable, message)
                    LogLevel.ERROR -> Timber.tag(tag).e(throwable, message)
                }
            }

            // File logging
            synchronized(this) {
                val logFile = File(logDir, getCurrentLogFileName())
                FileWriter(logFile, true).use { writer ->
                    writer.append(fullMessage)
                }
            }
        } catch (e: Exception) {
            Timber.tag("FileLogger").e(e, "Error writing log")
        }
    }

    private fun getCurrentLogFileName(): String {
        val dateStr = fileDateFormat.format(Date())
        return "$LOG_FILE_PREFIX-$dateStr.log"
    }

    private fun cleanOldLogs() {
        try {
            val now = System.currentTimeMillis()
            val maxAge = MAX_LOG_AGE_DAYS * 24 * 60 * 60 * 1000

            logDir.listFiles()?.forEach { file ->
                if (now - file.lastModified() > maxAge) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            Timber.tag("FileLogger").e(e, "Error cleaning old logs")
        }
    }

    suspend fun getLogFiles(): List<LogFile> = withContext(Dispatchers.IO) {
        logDir.listFiles()?.map { file ->
            LogFile(
                name = file.name,
                size = file.length(),
                lastModified = Date(file.lastModified()),
                path = file.absolutePath
            )
        } ?: emptyList()
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        try {
            logDir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            Timber.tag("FileLogger").e(e, "Error clearing logs")
        }
    }

    suspend fun getLogContent(fileName: String): String = withContext(Dispatchers.IO) {
        try {
            File(logDir, fileName).readText()
        } catch (e: Exception) {
            Timber.tag("FileLogger").e(e, "Error reading log file")
            ""
        }
    }

    data class LogFile(
        val name: String,
        val size: Long,
        val lastModified: Date,
        val path: String
    )
}