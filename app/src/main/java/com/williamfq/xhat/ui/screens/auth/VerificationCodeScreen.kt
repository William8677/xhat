package com.williamfq.xhat.ui.screens.auth

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.williamfq.xhat.R
import com.williamfq.xhat.ui.Navigation.Screen
import com.williamfq.xhat.ui.screens.auth.models.AuthState
import com.williamfq.xhat.ui.screens.auth.viewmodel.VerificationCodeViewModel
import com.williamfq.xhat.ui.theme.XhatTheme

// Definición del color naranja de Xhat
val XhatPrimaryColor = Color(0xFFF86320)

@SuppressLint("ContextCastToActivity")
@Composable
fun VerificationCodeScreen(
    navController: NavController,
    phoneNumber: String,
    verificationId: String,
    viewModel: VerificationCodeViewModel = hiltViewModel()
) {
    // Obtener la Activity actual para pasarla a la función de reenvío
    val activity = LocalContext.current as? Activity

    // Estado para el OTP (One Time Password)
    var otpCode by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.authState.collect { state ->
            when (state) {
                is AuthState.Success -> {
                    // Navegar a ProfileSetupScreen (o Main según tu lógica)
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.PhoneNumber.route) { inclusive = true }
                    }
                }
                else -> { /* otros estados, no se hace nada */ }
            }
        }
    }

    VerificationCodeContent(
        phoneNumber = phoneNumber,
        otp = otpCode,
        isLoading = viewModel.uiState.collectAsState().value.isLoading,
        error = viewModel.uiState.collectAsState().value.error,
        onOtpChange = { otpCode = it },
        onVerifyClicked = { code -> viewModel.verifyCode(code) },
        onResendClicked = {
            // Llama a la función de reenvío usando la Activity actual
            activity?.let { act ->
                viewModel.resendCode(phoneNumber, act)
            }
        }
    )
}

@Composable
private fun VerificationCodeContent(
    phoneNumber: String,
    otp: String,
    isLoading: Boolean,
    error: String?,
    onOtpChange: (String) -> Unit,
    onVerifyClicked: (String) -> Unit,
    onResendClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Image(
            painter = painterResource(id = R.drawable.logo_xhat_orange),
            contentDescription = "Logo de Xhat",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Verificación",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Introduce el código enviado al\n$phoneNumber",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campo OTP personalizado que muestra 6 cuadros centrados
        OtpInputField(
            otp = otp,
            onOtpChange = onOtpChange,
            isError = error != null,
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onVerifyClicked(otp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = otp.length == 6 && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = XhatPrimaryColor)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(text = "Verificar", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onResendClicked,
            enabled = !isLoading
        ) {
            Text(text = "Reenviar código", fontSize = 14.sp, color = XhatPrimaryColor)
        }
    }
}

@Composable
fun OtpInputField(
    otp: String,
    onOtpChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    // Campo de texto oculto para capturar el input
    Box(modifier = modifier) {
        BasicTextField(
            value = otp,
            onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    onOtpChange(it)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .height(0.dp)
                .alpha(0f)
        )
        // Mostrar cada dígito en un cuadro, centrados horizontalmente
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (i in 0 until 6) {
                val char = if (i < otp.length) otp[i].toString() else ""
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 2.dp,
                            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondary,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(text = char, style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VerificationCodeScreenPreview() {
    XhatTheme {
        VerificationCodeContent(
            phoneNumber = "+1234567890",
            otp = "",
            isLoading = false,
            error = null,
            onOtpChange = {},
            onVerifyClicked = {},
            onResendClicked = {}
        )
    }
}
