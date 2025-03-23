package com.williamfq.xhat.utils.logging

import android.util.Log

interface LoggerInterface {
    suspend fun logEvent(
        tag: String,
        message: String,
        level: LogLevel,
        throwable: Throwable? = null
    )
}

enum class LogLevel(val priority: Int) {
    VERBOSE(Log.VERBOSE),
    DEBUG(Log.DEBUG),
    INFO(Log.INFO),
    WARNING(Log.WARN),
    ERROR(Log.ERROR);

    companion object {
        fun fromPriority(priority: Int): LogLevel {
            return when (priority) {
                Log.VERBOSE -> VERBOSE
                Log.DEBUG -> DEBUG
                Log.INFO -> INFO
                Log.WARN -> WARNING
                Log.ERROR -> ERROR
                else -> ERROR
            }
        }
    }
}