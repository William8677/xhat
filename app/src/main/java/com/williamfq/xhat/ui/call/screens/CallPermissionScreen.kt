package com.williamfq.xhat.ui.call.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.williamfq.xhat.call.permissions.CallPermission

@Composable
fun CallPermissionScreen(
    missingPermissions: List<CallPermission>,
    onRequestPermissions: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = when {
                missingPermissions.contains(CallPermission.CAMERA) -> Icons.Default.Videocam
                else -> Icons.Default.Call
            },
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Permisos necesarios",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Para realizar llamadas, necesitamos los siguientes permisos:",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        missingPermissions.forEach { permission ->
            PermissionItem(permission)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(
                onClick = onCancel
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = onRequestPermissions
            ) {
                Text("Conceder permisos")
            }
        }
    }
}

@Composable
private fun PermissionItem(permission: CallPermission) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (permission) {
                CallPermission.MICROPHONE -> Icons.Default.Mic
                CallPermission.CAMERA -> Icons.Default.Videocam
                CallPermission.NOTIFICATIONS -> Icons.Default.Notifications
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = permission.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = permission.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}