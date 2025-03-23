package com.williamfq.xhat

import android.Manifest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor() : ViewModel() {

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted

    private val _permissionsStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val permissionsStatus: StateFlow<Map<String, Boolean>> = _permissionsStatus

    private var requestPermissionsLauncher: ((Array<String>) -> Unit)? = null

    val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    init {
        Timber.d("PermissionsViewModel inicializado")
    }

    fun initializePermissionsLauncher(launcher: (Array<String>) -> Unit) {
        requestPermissionsLauncher = launcher
        Timber.d("Launcher de permisos inicializado")
    }

    fun updatePermissionsStatus(permissions: Map<String, Boolean>) {
        _permissionsStatus.value = permissions
        val allGranted = permissions.values.all { it }
        _permissionsGranted.value = allGranted
        Timber.d("Estado de permisos actualizado: todos concedidos = $allGranted")

        permissions.forEach { (permission, isGranted) ->
            Timber.v("Permiso $permission: ${if (isGranted) "concedido" else "denegado"}")
        }
    }

    fun requestPermissionsIfNeeded() {
        if (!_permissionsGranted.value) {
            requestPermissionsLauncher?.invoke(requiredPermissions)
                ?: Timber.e("Error: El launcher de permisos no está inicializado")
        } else {
            Timber.d("Los permisos ya están concedidos")
        }
    }

    fun isPermissionGranted(permission: String): Boolean {
        return _permissionsStatus.value[permission] ?: false
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("PermissionsViewModel destruido")
    }

    fun cleanup() {
        viewModelScope.cancel()
        requestPermissionsLauncher = null
        Timber.d("Recursos limpiados en cleanup")
    }
}