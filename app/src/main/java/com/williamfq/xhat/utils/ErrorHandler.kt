// utils/ErrorHandler.kt
package com.williamfq.xhat.utils

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorHandler @Inject constructor(
    private val logUtils: LogUtils
) {
    fun handleException(exception: Exception) {
        logUtils.logError("Error detectado: ${exception.message}", exception)
        // Aquí puedes agregar lógica adicional como:
        // - Enviar el error a un servicio de monitoreo
        // - Guardar el error en una base de datos local
        // - Mostrar un diálogo específico según el tipo de error
    }
}