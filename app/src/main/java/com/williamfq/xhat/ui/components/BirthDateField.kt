package com.williamfq.xhat.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BirthDateField(
    birthDate: String,
    error: String?,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = birthDate,
        onValueChange = { },
        label = { Text("Fecha de nacimiento") },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        enabled = false // Campo de solo lectura, activado mediante DatePicker
    )
}
