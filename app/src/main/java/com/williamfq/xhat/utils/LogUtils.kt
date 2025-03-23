package com.williamfq.xhat.utils

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogUtils @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val LOG_FILE = "xhat_detailed_log.txt"
        private const val ERROR_LOG_FILE = "xhat_errors.txt"
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        private const val CURRENT_USER = "William8677"
        private const val CURRENT_UTC = "2025-01-11 14:08:29"
        private val errorCountMap = ConcurrentHashMap<String, Int>()
    }

    /**
     * 1) Se crea una propiedad `lazy` para obtener el appVersion
     *    usando el `context` de la clase externa.
     *    Esto evita el uso directo de `context` dentro de la data class anidada.
     */
    private val appVersionForDevice by lazy {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            "unknown"
        }
    }

    /**
     * Data class principal de la información detallada.
     */
    data class DetailedLogInfo(
        val timestamp: String = LocalDateTime.now().format(dateFormatter),
        val utcTimestamp: String = LocalDateTime.now(ZoneOffset.UTC).format(dateFormatter),
        val className: String,
        val methodName: String,
        val lineNumber: Int,
        val user: String = CURRENT_USER,
        val taskId: String? = null,
        val threadInfo: ThreadInfo = ThreadInfo(),
        /**
         * 2) Aquí usamos el constructor de `DeviceInfo` para inyectar
         *    el `appVersionForDevice` que se obtuvo arriba.
         */
        val deviceInfo: DeviceInfo = DeviceInfo(
            appVersion = "unknown" // Valor por defecto; se ajustará más abajo
        ),
        val extraInfo: Map<String, Any?> = emptyMap()
    ) {
        data class ThreadInfo(
            val threadName: String = Thread.currentThread().name,
            val threadId: Long = Thread.currentThread().id,
            val isMainThread: Boolean = (Thread.currentThread().name == "main")
        )

        /**
         * Data class anidada para la información de dispositivo.
         *
         * Nota: no podemos acceder a `context` directamente desde aquí,
         * por eso aprovechamos `appVersionForDevice` definido en `LogUtils`.
         */
        data class DeviceInfo(
            val manufacturer: String = Build.MANUFACTURER,
            val model: String = Build.MODEL,
            val sdkVersion: Int = Build.VERSION.SDK_INT,

            // Esta línea es la original:
            // val appVersion: String = context?.packageName?.let { ... } ?: "unknown"
            // La ajustamos para inyectar el valor desde fuera (appVersionForDevice).
            val appVersion: String
        )

        fun toFormattedString(): String = buildString {
            append("══════════════════════════════════════\n")
            append("📱 Log Entry Details:\n")
            append("══════════════════════════════════════\n")
            append("⏰ Local Time: $timestamp\n")
            append("🌐 UTC Time: $utcTimestamp\n")
            append("👤 User: $user\n")
            append("📍 Location: $className.$methodName (line: $lineNumber)\n")
            taskId?.let { append("🔑 Task ID: $it\n") }
            append("\n🧵 Thread Info:\n")
            append("   Name: ${threadInfo.threadName}\n")
            append("   ID: ${threadInfo.threadId}\n")
            append("   Main Thread: ${threadInfo.isMainThread}\n")
            append("\n📱 Device Info:\n")
            append("   Manufacturer: ${deviceInfo.manufacturer}\n")
            append("   Model: ${deviceInfo.model}\n")
            append("   Android SDK: ${deviceInfo.sdkVersion}\n")
            append("   App Version: ${deviceInfo.appVersion}\n")
            if (extraInfo.isNotEmpty()) {
                append("\n📎 Additional Info:\n")
                extraInfo.forEach { (key, value) ->
                    append("   $key: $value\n")
                }
            }
            append("══════════════════════════════════════")
        }
    }

    init {
        // Plantamos un Timber DebugTree personalizado si no hay otro.
        if (Timber.forest().isEmpty()) {
            Timber.plant(object : Timber.DebugTree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    super.log(priority, tag, message, t)
                    logToFile(priority, tag, message, t)
                    if (priority == Log.ERROR) {
                        logErrorToSeparateFile(tag, message, t)
                    }
                }

                override fun createStackElementTag(element: StackTraceElement): String {
                    return "(${element.fileName}:${element.lineNumber})#${element.methodName}"
                }
            })
        }
    }

    /**
     * 3) Ajustamos el método que crea un `DetailedLogInfo`.
     *    Inyectamos `appVersionForDevice` dentro de `deviceInfo`.
     */
    private fun createDetailedLogInfo(): DetailedLogInfo {
        val stackTrace = Thread.currentThread().stackTrace
        val caller = stackTrace.firstOrNull {
            it.className != this::class.java.name && !it.className.contains("java.lang.Thread")
        }
        return DetailedLogInfo(
            className = caller?.className?.substringAfterLast('.') ?: "Unknown",
            methodName = caller?.methodName ?: "unknown",
            lineNumber = caller?.lineNumber ?: -1,
            deviceInfo = DetailedLogInfo.DeviceInfo(
                manufacturer = Build.MANUFACTURER,
                model = Build.MODEL,
                sdkVersion = Build.VERSION.SDK_INT,
                // (CORRECCIÓN) Forzamos a que sea String no nulo.
                appVersion = appVersionForDevice ?: "unknown"
            )
        )
    }

    private fun logToFile(priority: Int, tag: String?, message: String, t: Throwable?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val logFile = File(context.getExternalFilesDir(null), LOG_FILE)
                val priorityStr = when (priority) {
                    Log.VERBOSE -> "📝 VERBOSE"
                    Log.DEBUG -> "🔍 DEBUG"
                    Log.INFO -> "ℹ️ INFO"
                    Log.WARN -> "⚠️ WARNING"
                    Log.ERROR -> "❌ ERROR"
                    else -> "❓ UNKNOWN"
                }

                val logEntry = buildString {
                    append("$priorityStr | ${LocalDateTime.now().format(dateFormatter)} UTC\n")
                    append("📌 Tag: ${tag ?: "NoTag"}\n")
                    append("📄 Message: $message\n")
                    if (t != null) {
                        append("⚠️ Exception Details:\n")
                        append("   Type: ${t.javaClass.name}\n")
                        append("   Message: ${t.message}\n")
                        append("   Stack Trace:\n")
                        t.stackTrace.forEach { element ->
                            append("      at $element\n")
                        }
                    }
                    append("\n")
                }

                logFile.appendText(logEntry)
            } catch (e: Exception) {
                Timber.tag("LogUtils").e(e, "Error writing to log file")
            }
        }
    }

    /**
     * (CAMBIO) Agregamos más detalle en los errores.
     */
    private fun logErrorToSeparateFile(tag: String?, message: String, t: Throwable?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // (CAMBIO) Creamos un logInfo extra para añadir detalles del lugar del error.
                val logInfo = createDetailedLogInfo()

                val errorFile = File(context.getExternalFilesDir(null), ERROR_LOG_FILE)
                val errorKey = "${t?.javaClass?.simpleName ?: "Unknown"}_${message.hashCode()}"
                val errorCount = errorCountMap.compute(errorKey) { _, count -> (count ?: 0) + 1 } ?: 1

                val errorEntry = buildString {
                    append("═══════ Error Report ═══════\n")
                    append("🕒 Time: ${LocalDateTime.now().format(dateFormatter)}\n")
                    append("📱 Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
                    append("🤖 Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n")
                    append("👤 User: $CURRENT_USER\n")
                    append("🔄 Occurrence: #$errorCount\n")
                    append("🏷️ Tag: $tag\n")
                    append("❌ Error: $message\n")
                    // (CAMBIO) Más detalles del stack donde ocurrió:
                    append("📍 Class: ${logInfo.className}\n")
                    append("📍 Method: ${logInfo.methodName}\n")
                    append("📍 Line: ${logInfo.lineNumber}\n")
                    if (t != null) {
                        append("📚 Stack Trace:\n")
                        t.stackTrace.forEach { element ->
                            append("   at $element\n")
                        }
                    }
                    append("════════════════════════════\n\n")
                }

                errorFile.appendText(errorEntry)
            } catch (e: Exception) {
                Timber.tag("LogUtils").e(e, "Error writing to error log file")
            }
        }
    }

    fun logDebug(message: String, info: DetailedLogInfo? = null) {
        val logInfo = info ?: createDetailedLogInfo()
        Timber.d("${logInfo.toFormattedString()}\n🔍 Debug Message: $message")
    }

    fun logInfo(message: String, info: DetailedLogInfo? = null) {
        val logInfo = info ?: createDetailedLogInfo()
        Timber.i("${logInfo.toFormattedString()}\nℹ️ Info: $message")
    }

    fun logWarning(message: String, info: DetailedLogInfo? = null) {
        val logInfo = info ?: createDetailedLogInfo()
        Timber.w("${logInfo.toFormattedString()}\n⚠️ Warning: $message")
    }

    fun logError(message: String, error: Throwable? = null, info: DetailedLogInfo? = null) {
        val logInfo = info ?: createDetailedLogInfo()
        val errorDetails = error?.let {
            buildString {
                append("\n❌ Exception Details:\n")
                append("   Type: ${it.javaClass.name}\n")
                append("   Message: ${it.message}\n")
                append("   Cause: ${it.cause?.message ?: "Unknown"}\n")
                append("   Stack Trace:\n")
                it.stackTrace.forEach { element ->
                    append("      at $element\n")
                }
            }
        } ?: "\n❌ No exception details available"

        Timber.e("${logInfo.toFormattedString()}\n🚫 Error Message: $message$errorDetails")
    }

    fun logSuccess(message: String, info: DetailedLogInfo? = null) {
        val logInfo = info ?: createDetailedLogInfo()
        Timber.i("${logInfo.toFormattedString()}\n✅ Success: $message")
    }

    fun logResourceError(
        resourceId: Int,
        resourceType: String,
        expectedValue: String? = null, // Permite null, pero podría provocar un 'type mismatch' si se fuerza a String en otro lado
        callerInfo: DetailedLogInfo? = null
    ) {
        val info = callerInfo ?: createDetailedLogInfo()
        try {
            val resourceName = try {
                context.resources.getResourceEntryName(resourceId)
            } catch (e: Resources.NotFoundException) {
                "unknown_resource"
            }

            val resourceValue = try {
                when (resourceType) {
                    "string" -> context.getString(resourceId)
                    "drawable" -> "drawable/${context.resources.getResourceEntryName(resourceId)}"
                    "layout" -> "layout/${context.resources.getResourceEntryName(resourceId)}"
                    else -> "unknown_type"
                }
            } catch (e: Resources.NotFoundException) {
                "not_found"
            }

            val extraInfo = info.extraInfo.toMutableMap().apply {
                put("resource_id", resourceId)
                put("resource_name", resourceName)
                put("resource_type", resourceType)
                put("current_value", resourceValue)
                // (CAMBIO) Aseguramos que si expectedValue es null no se genere conflicto.
                expectedValue?.let { safeValue ->
                    put("expected_value", safeValue)
                }
            }

            val resourceException = Resources.NotFoundException(
                "Resource not found: $resourceType/$resourceName"
            )

            logError(
                "Resource Error: $resourceType/$resourceName",
                resourceException,
                info.copy(extraInfo = extraInfo)
            )
        } catch (e: Exception) {
            logError("Error accessing resource", e, info)
        }
    }

    fun getAllLogs(filterLevel: String? = null, maxLines: Int = 100): List<String> {
        return try {
            val logFile = File(context.getExternalFilesDir(null), LOG_FILE)
            if (!logFile.exists()) return emptyList()

            logFile.readLines()
                .asReversed()
                .filter { line ->
                    filterLevel == null || line.contains("| $filterLevel |")
                }
                .take(maxLines)
        } catch (e: Exception) {
            logError("Error reading logs", e)
            emptyList()
        }
    }

    fun searchLogs(searchTerm: String, maxResults: Int = 50): List<String> {
        return try {
            val logFile = File(context.getExternalFilesDir(null), LOG_FILE)
            if (!logFile.exists()) return emptyList()

            logFile.readLines()
                .filter { it.contains(searchTerm, ignoreCase = true) }
                .takeLast(maxResults)
        } catch (e: Exception) {
            logError("Error searching logs", e)
            emptyList()
        }
    }

    fun getErrorStatistics(): Map<String, Int> {
        return errorCountMap.toMap()
    }
}
