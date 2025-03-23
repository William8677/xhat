/*
 * Updated: 2025-01-21 01:05:15 UTC
 * Author: William8677
 */
package com.williamfq.xhat.ui.screens.auth.components

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.williamfq.xhat.ui.screens.auth.models.ProfileSetupState
import com.williamfq.xhat.ui.theme.XhatTheme
import java.text.SimpleDateFormat
import java.util.*
import com.williamfq.xhat.ui.profile.*

@Composable
fun Modifier.xhatGradientBorder(
    width: Dp = 3.dp,
    shape: Shape = CircleShape
) = this.then(
    Modifier.border(
        width = width,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFF86320),
                Color(0xFFFF9800),
                Color(0xFFF44336)
            )
        ),
        shape = shape
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupContent(
    uiState: ProfileSetupState,
    profileState: ProfileState,
    profileImageUri: Uri?,
    coverImageUri: Uri?,
    onProfileStateChange: ((ProfileState) -> ProfileState) -> Unit,
    onSelectProfileImage: () -> Unit,
    onSelectCoverImage: () -> Unit,
    onSaveProfile: (
        name: String,
        description: String,
        country: String,
        state: String,
        city: String,
        birthDate: String
    ) -> Unit
)
 {
    val datePickerState = rememberDatePickerState()
    val imageAlpha by animateFloatAsState(
        targetValue = if (profileImageUri != null || coverImageUri != null) 1f else 0.6f
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .animateContentSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            // Cover Image con borde degradado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .xhatGradientBorder(width = 2.dp, shape = RectangleShape)
                    .background(Color.LightGray)
                    .alpha(imageAlpha)
                    .clickable(onClick = onSelectCoverImage)
            ) {
                coverImageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Imagen de portada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    IconButton(
                        onClick = onSelectCoverImage,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF86320).copy(alpha = 0.9f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Cambiar foto de portada",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Profile Image con borde degradado
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 90.dp)
                    .clip(CircleShape)
                    .xhatGradientBorder()
                    .background(Color.White)
                    .alpha(imageAlpha)
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUri != null) {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Foto de perfil",
                        tint = Color.Gray,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            // Botón de foto de perfil
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 170.dp, x = 35.dp)
                    .zIndex(2f)
                    .background(Color(0xFFF86320).copy(alpha = 0.9f), CircleShape)
                    .clickable(onClick = onSelectProfileImage),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Cambiar foto de perfil",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        // Campos del formulario
        ProfileFormFields(
            profileState = profileState,
            uiState = uiState,
            onProfileStateChange = onProfileStateChange,
            datePickerState = datePickerState
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de guardar (ahora sin pasar parámetros)
        SaveButton(
            uiState = uiState,
            profileState = profileState,
            onSaveProfile = onSaveProfile
        )

        // Mensaje de error
        if (uiState is ProfileSetupState.Error) {
            ErrorMessage(message = uiState.message)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileFormFields(
    profileState: ProfileState,
    uiState: ProfileSetupState,
    onProfileStateChange: ((ProfileState) -> ProfileState) -> Unit,
    datePickerState: DatePickerState
) {
    OutlinedTextField(
        value = profileState.name,
        onValueChange = { newValue -> onProfileStateChange { it.copy(name = newValue) } },
        label = { Text("Nombre") },
        modifier = Modifier.fillMaxWidth(),
        isError = uiState is ProfileSetupState.Error && profileState.name.length < 3
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = profileState.description,
        onValueChange = { newValue -> onProfileStateChange { it.copy(description = newValue) } },
        label = { Text("Descripción") },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 3,
        isError = uiState is ProfileSetupState.Error && profileState.description.length > 500
    )

    Spacer(modifier = Modifier.height(8.dp))

    LocationFields(profileState)

    Spacer(modifier = Modifier.height(8.dp))

    DatePickerField(
        profileState = profileState,
        datePickerState = datePickerState,
        onProfileStateChange = onProfileStateChange
    )
}

@Composable
private fun LocationFields(profileState: ProfileState) {
    OutlinedTextField(
        value = profileState.country,
        onValueChange = { },
        label = { Text("País") },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = profileState.state,
        onValueChange = { },
        label = { Text("Estado/Provincia") },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = profileState.city,
        onValueChange = { },
        label = { Text("Ciudad") },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    profileState: ProfileState,
    datePickerState: DatePickerState,
    onProfileStateChange: ((ProfileState) -> ProfileState) -> Unit
) {
    OutlinedTextField(
        value = profileState.birthDate,
        onValueChange = { },
        label = { Text("Fecha de nacimiento") },
        readOnly = true,
        trailingIcon = {
            IconButton(
                onClick = { onProfileStateChange { it.copy(showDatePicker = true) } }
            ) {
                Icon(Icons.Default.DateRange, "Seleccionar fecha")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    if (profileState.showDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                onProfileStateChange { it.copy(showDatePicker = false) }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val newDate = SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            ).format(Date(it))
                            onProfileStateChange {
                                it.copy(
                                    birthDate = newDate,
                                    showDatePicker = false
                                )
                            }
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onProfileStateChange { it.copy(showDatePicker = false) }
                    }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SaveButton(
    uiState: ProfileSetupState,
    profileState: ProfileState,
    onSaveProfile: (String, String, String, String, String, String) -> Unit
) {
    Button(
        onClick = {
            onSaveProfile(
                profileState.name,
                profileState.description,
                profileState.country,
                profileState.state,
                profileState.city,
                profileState.birthDate
            )
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = uiState !is ProfileSetupState.Loading,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF86320))
    ) {
        if (uiState is ProfileSetupState.Loading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text("Guardar", color = Color.White)
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileSetupContentPreview() {
    XhatTheme {
        ProfileSetupContent(
            uiState = ProfileSetupState.Initial,
            profileState = ProfileState(
                name = "",
                description = "",
                country = "México",
                state = "Jalisco",
                city = "Guadalajara"
            ),
            profileImageUri = null,
            coverImageUri = null,
            onProfileStateChange = { state -> state },
            onSelectProfileImage = { },
            onSelectCoverImage = { },
            onSaveProfile = { _, _, _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileSetupContentErrorPreview() {
    XhatTheme {
        ProfileSetupContent(
            uiState = ProfileSetupState.Error("Error de ejemplo"),
            profileState = ProfileState(
                name = "Test",
                description = "Test description",
                country = "México",
                state = "Jalisco",
                city = "Guadalajara"
            ),
            profileImageUri = null,
            coverImageUri = null,
            onProfileStateChange = { state -> state },
            onSelectProfileImage = { },
            onSelectCoverImage = { },
            onSaveProfile = { _, _, _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileSetupContentLoadingPreview() {
    XhatTheme {
        ProfileSetupContent(
            uiState = ProfileSetupState.Loading,
            profileState = ProfileState(
                name = "Test",
                description = "Test description",
                country = "México",
                state = "Jalisco",
                city = "Guadalajara"
            ),
            profileImageUri = null,
            coverImageUri = null,
            onProfileStateChange = { state -> state },
            onSelectProfileImage = { },
            onSelectCoverImage = { },
            onSaveProfile = { _, _, _, _, _, _ -> }
        )
    }
}
