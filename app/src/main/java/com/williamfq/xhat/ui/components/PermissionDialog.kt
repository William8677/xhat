@file:OptIn(ExperimentalPermissionsApi::class)

package com.williamfq.xhat.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.williamfq.xhat.R

@Composable
fun PermissionDialog(
    permissions: List<String>,
    showDialog: Boolean,
    onPermissionResult: (Boolean) -> Unit
) {
    val permissionsState = rememberMultiplePermissionsState(permissions)

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onPermissionResult(false) },
            title = { Text(stringResource(R.string.permission_required)) },
            text = { Text(stringResource(R.string.location_permission_rationale)) },
            confirmButton = {
                TextButton(
                    onClick = { permissionsState.launchMultiplePermissionRequest() }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { onPermissionResult(false) }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            onPermissionResult(true)
        }
    }
}
