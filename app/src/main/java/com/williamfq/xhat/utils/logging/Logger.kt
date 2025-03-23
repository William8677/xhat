package com.williamfq.xhat.utils.logging

import android.util.Log
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Logger @Inject constructor(
    private val loggerInterface: LoggerInterface
) {
    private val logQueue = ConcurrentLinkedQueue<LogEntry>()
    private val logScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isProcessingQueue = false

    init {
        startQueueProcessor()
    }

    suspend fun v(tag: String, message: String, throwable: Throwable? = null) {
        enqueueLog(LogLevel.VERBOSE, tag, message, throwable)
    }

    suspend fun d(tag: String, message: String, throwable: Throwable? = null) {
        enqueueLog(LogLevel.DEBUG, tag, message, throwable)
    }

    suspend fun i(tag: String, message: String, throwable: Throwable? = null) {
        enqueueLog(LogLevel.INFO, tag, message, throwable)
    }

    suspend fun w(tag: String, message: String, throwable: Throwable? = null) {
        enqueueLog(LogLevel.WARNING, tag, message, throwable)
    }

    suspend fun e(tag: String, message: String, throwable: Throwable? = null) {
        enqueueLog(LogLevel.ERROR, tag, message, throwable)
    }

    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        runBlocking {
            enqueueLog(level, tag, message, throwable)
        }
    }

    private fun enqueueLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val logEntry = LogEntry(level, tag, message, throwable, System.currentTimeMillis())
        logQueue.offer(logEntry)
        if (!isProcessingQueue) {
            startQueueProcessor()
        }
    }

    private fun startQueueProcessor() {
        if (isProcessingQueue) return

        isProcessingQueue = true
        logScope.launch {
            while (isActive) {
                val entry = logQueue.poll()
                if (entry != null) {
                    try {
                        loggerInterface.logEvent(entry.tag, entry.message, entry.level, entry.throwable)
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "Error processing log entry")
                    }
                } else {
                    isProcessingQueue = false
                    break
                }
                delay(LOG_PROCESS_DELAY)
            }
        }
    }

    fun destroy() {
        logScope.cancel()
    }

    private data class LogEntry(
        val level: LogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable?,
        val timestamp: Long
    )

    companion object {
        private const val TAG = "Logger"
        private const val LOG_PROCESS_DELAY = 50L // ms
    }
}