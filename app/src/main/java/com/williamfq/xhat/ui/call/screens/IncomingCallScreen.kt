package com.williamfq.xhat.ui.call.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.williamfq.xhat.ui.call.viewmodel.IncomingCallViewModel

@Composable
fun IncomingCallScreen(
    callId: String,
    viewModel: IncomingCallViewModel = hiltViewModel()
) {
    val callInfo by viewModel.callInfo.collectAsState()

    LaunchedEffect(callId) {
        viewModel.loadCallInfo(callId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Información del llamante
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (callInfo.isVideoCall)
                    Icons.Default.VideoCall
                else Icons.Default.Call,
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = callInfo.callerName,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = if (callInfo.isVideoCall) "Videollamada entrante"
                else "Llamada entrante",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Botones de acción
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Botón rechazar
            IconButton(
                onClick = { viewModel.rejectCall() },
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "Rechazar llamada",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Botón aceptar
            IconButton(
                onClick = { viewModel.acceptCall() },
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Icon(
                    imageVector = if (callInfo.isVideoCall)
                        Icons.Default.VideoCall
                    else Icons.Default.Call,
                    contentDescription = "Aceptar llamada",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}