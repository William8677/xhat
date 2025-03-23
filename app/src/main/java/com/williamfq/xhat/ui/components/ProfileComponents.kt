@file:OptIn(ExperimentalMaterial3Api::class)

package com.williamfq.xhat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun UsernameField(
    username: String,
    error: String?,
    onUsernameChange: (String) -> Unit
) {
    OutlinedTextField(
        value = username,
        onValueChange = { value ->
            onUsernameChange(value.replace(Regex("[^a-zA-Z0-9_]"), ""))
        },
        label = { Text("Nombre de usuario") },
        prefix = { Text("@") },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun NameField(
    name: String,
    error: String?,
    onNameChange: (String) -> Unit
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Nombre") },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun DescriptionField(
    description: String,
    error: String?,
    onDescriptionChange: (String) -> Unit
) {
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { Text("Descripción") },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5
    )
}

@Composable
fun LocationFields(
    country: String,
    state: String,
    city: String,
    onLocationClick: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = country,
            onValueChange = { },
            label = { Text("País") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state,
            onValueChange = { },
            label = { Text("Estado/Provincia") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = city,
            onValueChange = { },
            label = { Text("Ciudad") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ProfileDatePicker(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let(onDateSelected)
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun LocationPermissionDialog(
    onAllow: () -> Unit,
    onDeny: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDeny,
        title = { Text("Permiso de ubicación") },
        text = {
            Text("Para obtener tu ubicación automáticamente, necesitamos acceso a tu ubicación.")
        },
        confirmButton = {
            TextButton(onClick = onAllow) {
                Text("Permitir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDeny) {
                Text("Denegar")
            }
        }
    )
}
