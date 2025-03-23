/*
 * Updated: 2025-01-27 02:50:48
 * Author: William8677
 */

package com.williamfq.xhat.panic

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.domain.location.LocationTracker
import com.williamfq.xhat.R
import com.williamfq.xhat.ui.Navigation.ChatType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun PanicButton(
    chatId: String,
    chatType: ChatType,
    onActivate: (String, ChatType) -> Unit,
    viewModel: PanicButtonViewModel = hiltViewModel()
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    val panicState by viewModel.panicState.collectAsState()

    FloatingActionButton(
        onClick = { showConfirmDialog = true },
        containerColor = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = stringResource(R.string.panic_button),
            tint = MaterialTheme.colorScheme.onError
        )
    }

    if (showConfirmDialog) {
        PanicConfirmationDialog(
            onConfirm = {
                showConfirmDialog = false
                viewModel.activatePanicMode(chatId, chatType)
                onActivate(chatId, chatType)
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    // Mostrar errores si los hay
    LaunchedEffect(panicState) {
        if (panicState is PanicState.Error) {
            // Aquí podrías mostrar un Snackbar o AlertDialog con el error
        }
    }
}

@Composable
private fun PanicConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.panic_mode_title)) },
        text = { Text(stringResource(R.string.panic_mode_confirmation)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.activate))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@HiltViewModel
class PanicButtonViewModel @Inject constructor(
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _panicState = MutableStateFlow<PanicState>(PanicState.Inactive)
    val panicState = _panicState.asStateFlow()

    fun activatePanicMode(chatId: String, chatType: ChatType) {
        viewModelScope.launch {
            try {
                _panicState.value = PanicState.Active
                locationTracker.getCurrentLocation()
                    .catch { e ->
                        _panicState.value = PanicState.Error(e.message ?: "Error desconocido")
                    }
                    .collect { location ->
                        if (location != null) {
                            sendLocationUpdate(chatId, chatType, location)
                        } else {
                            _panicState.value = PanicState.Error("Ubicación no disponible")
                        }
                    }
            } catch (e: Exception) {
                _panicState.value = PanicState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    private suspend fun sendLocationUpdate(
        chatId: String,
        chatType: ChatType,
        location: com.williamfq.domain.models.Location?
    ) {
        try {
            // TODO: Implementar la lógica para enviar la ubicación
            // Por ejemplo:
            // locationRepository.sendLocation(chatId, chatType, location)
        } catch (e: Exception) {
            _panicState.value = PanicState.Error(e.message ?: "Error al enviar la ubicación")
        }
    }

    fun deactivatePanicMode() {
        _panicState.value = PanicState.Inactive
    }
}

sealed class PanicState {
    object Inactive : PanicState()
    object Active : PanicState()
    data class Error(val message: String) : PanicState()
}
