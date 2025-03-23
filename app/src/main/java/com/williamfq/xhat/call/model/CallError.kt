/*
 * Updated: 2025-01-27 03:57:57
 * Author: William8677
 */

package com.williamfq.xhat.call.model

sealed class CallError : Exception() {
    data class AudioError(override val message: String) : CallError()
    data class NetworkError(override val message: String) : CallError()
    data class ServiceError(override val message: String) : CallError()
    data class PermissionError(override val message: String) : CallError()
    data class InitializationError(override val message: String) : CallError()

    fun toErrorCode(): CallErrorCode = when (this) {
        is AudioError -> CallErrorCode.MEDIA_ERROR
        is NetworkError -> CallErrorCode.NETWORK_ERROR
        is ServiceError -> CallErrorCode.SERVICE_UNAVAILABLE
        is PermissionError -> CallErrorCode.PERMISSION_DENIED
        is InitializationError -> CallErrorCode.INTERNAL_ERROR
    }
}