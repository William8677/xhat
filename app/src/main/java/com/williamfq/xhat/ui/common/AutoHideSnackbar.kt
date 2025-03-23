package com.williamfq.xhat.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AutoHideSnackbar(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier,
        // Usamos el parámetro "snackbar" para personalizar el contenido del Snackbar
        snackbar = { snackbarData ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Se obtiene el mensaje a través de visuals.message
                Text(text = snackbarData.visuals.message)
                TextButton(onClick = { snackbarData.dismiss() }) {
                    Text("Cerrar")
                }
            }
        }
    )
}
