package com.williamfq.xhat.ui.screens.auth

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.williamfq.domain.model.Country
import com.williamfq.xhat.R
import com.williamfq.xhat.ui.Navigation.Screen
import com.williamfq.xhat.ui.screens.auth.models.PhoneAuthState
import com.williamfq.xhat.ui.screens.auth.models.PhoneNumberUiState
import com.williamfq.xhat.ui.screens.auth.viewmodel.PhoneNumberViewModel
import com.williamfq.xhat.ui.theme.XhatTheme
import com.williamfq.xhat.utils.loadCountries

// Definición del color naranja de Xhat
val PrimaryColor = Color(0xFFF86320)

@SuppressLint("ContextCastToActivity")
@Composable
fun PhoneNumberScreen(
    navController: NavController,
    viewModel: PhoneNumberViewModel = hiltViewModel()
) {
    // Obtener la Activity actual para el flujo de verificación
    val activity = LocalContext.current as? Activity

    // Cargar la lista de países desde assets
    val context = LocalContext.current
    val countries = remember { loadCountries(context) }
    // Seleccionar país por defecto
    var selectedCountry by remember { mutableStateOf(countries.firstOrNull() ?: Country("Desconocido", "")) }
    // Estados para el código del país y el número de teléfono
    var countryCodeInput by remember { mutableStateOf(selectedCountry.code) }
    var phoneNumberInput by remember { mutableStateOf("") }

    // Función para actualizar el código del país y, si es posible, el país seleccionado
    fun onCountryCodeChange(newCode: String) {
        countryCodeInput = newCode
        // Se busca un país que coincida exactamente con el código ingresado (sin espacios)
        countries.find { it.code.replace(" ", "") == newCode.replace(" ", "") }?.let {
            selectedCountry = it
        }
    }

    LaunchedEffect(viewModel.authState) {
        viewModel.authState.collect { state ->
            when (state) {
                is PhoneAuthState.CodeSent -> {
                    // Combinar el código del país y el número local para formar el número completo
                    val fullNumber = countryCodeInput + phoneNumberInput.trim()
                    navController.navigate(
                        Screen.VerificationCode.createRoute(fullNumber, state.verificationId)
                    ) {
                        popUpTo(Screen.PhoneNumber.route) { inclusive = true }
                    }
                }
                is PhoneAuthState.Error -> {
                    // Se maneja el error a través del UI state
                }
                else -> {}
            }
        }
    }

    PhoneNumberContent(
        uiState = viewModel.uiState,
        countries = countries,
        selectedCountry = selectedCountry,
        countryCodeInput = countryCodeInput,
        onCountryCodeChange = { onCountryCodeChange(it) },
        phoneNumberInput = phoneNumberInput,
        onPhoneNumberChange = { phoneNumberInput = it },
        onPhoneNumberSubmitted = {
            val fullNumber = countryCodeInput + phoneNumberInput.trim()
            if (activity != null) {
                viewModel.startPhoneNumberVerification(fullNumber, activity)
            }
        }
    )
}

@Composable
private fun PhoneNumberContent(
    uiState: PhoneNumberUiState,
    countries: List<Country>,
    selectedCountry: Country,
    countryCodeInput: String,
    onCountryCodeChange: (String) -> Unit,
    phoneNumberInput: String,
    onPhoneNumberChange: (String) -> Unit,
    onPhoneNumberSubmitted: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Icon(
            painter = painterResource(id = R.drawable.logo_xhat_orange),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = PrimaryColor
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Bienvenido a Xhat",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ingresa tu número de teléfono para comenzar",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Row que contiene el campo de código de país y el campo de teléfono
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(100.dp)) {
                OutlinedTextField(
                    value = countryCodeInput,
                    onValueChange = onCountryCodeChange,
                    label = { Text("Código") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Seleccionar país",
                        tint = PrimaryColor
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    countries.forEach { country ->
                        DropdownMenuItem(
                            text = { Text("${country.name} (${country.code})") },
                            onClick = {
                                onCountryCodeChange(country.code)
                                expanded = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = phoneNumberInput,
                onValueChange = onPhoneNumberChange,
                label = { Text("Teléfono") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.weight(1f)
            )
        }

        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Al continuar, aceptas nuestros Términos y Política de Privacidad",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContinueButton(
            isEnabled = (countryCodeInput + phoneNumberInput).length >= 10 && !uiState.isLoading,
            isLoading = uiState.isLoading,
            onClick = {
                keyboardController?.hide()
                onPhoneNumberSubmitted()
            }
        )
    }
}

@Composable
private fun AnimatedContinueButton(
    isEnabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryColor,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
    ) {
        AnimatedContent(
            targetState = when {
                isLoading -> "Verificando..."
                isEnabled -> "Continuar"
                else -> "Ingresa un número válido"
            },
            transitionSpec = {
                fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
            }
        ) { text ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PhoneNumberScreenPreview() {
    XhatTheme {
        PhoneNumberContent(
            uiState = PhoneNumberUiState(),
            countries = listOf(
                Country("Afganistán", "+93"),
                Country("Albania", "+355"),
                Country("Argelia", "+213")
            ),
            selectedCountry = Country("Afganistán", "+93"),
            countryCodeInput = "+93",
            onCountryCodeChange = {},
            phoneNumberInput = "123456789",
            onPhoneNumberChange = {},
            onPhoneNumberSubmitted = {}
        )
    }
}
