package com.sinc.mobile.app.features.forgotpassword

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.ui.components.InfoDialog
import com.sinc.mobile.app.ui.components.LoadingOverlay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    isFirstTime: Boolean = false,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val snackbarHostState = remember { SnackbarHostState() }

    val screenTitle = when (state.step) {
        ForgotPasswordStep.EnterEmail -> if (isFirstTime) "Primer Ingreso" else "Recuperar Contraseña"
        ForgotPasswordStep.EnterCode -> "Verificar Código"
        ForgotPasswordStep.EnterNewPassword -> if (isFirstTime) "Establecer Contraseña" else "Nueva Contraseña"
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is ForgotPasswordViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }
                is ForgotPasswordViewModel.UiEvent.NavigateToLogin -> {
                    onNavigateToLogin()
                }
            }
        }
    }

    if (state.showSuccessDialog) {
        InfoDialog(
            showDialog = true,
            onDismiss = { viewModel.onSuccessDialogDismissed() },
            title = "Éxito",
            message = if (isFirstTime) 
                "Contraseña establecida con éxito. Ya puedes ingresar a tu cuenta." 
                else "Contraseña restablecida con éxito. Serás redirigido para que inicies sesión."
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            when (state.step) {
                                ForgotPasswordStep.EnterEmail -> onNavigateBack()
                                ForgotPasswordStep.EnterCode -> viewModel.onBackToEmail()
                                ForgotPasswordStep.EnterNewPassword -> viewModel.onBackToCode()
                            }
                        },
                        enabled = !state.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = state.step,
                label = "ForgotPasswordStepAnimation",
                modifier = Modifier.fillMaxSize()
            ) { targetStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (targetStep) {
                        ForgotPasswordStep.EnterEmail -> EnterEmailStep(
                            isLoading = state.isLoading,
                            isFirstTime = isFirstTime,
                            onSendCode = { email -> viewModel.onEmailEntered(email) }
                        )
                        ForgotPasswordStep.EnterCode -> EnterCodeStep(
                            isLoading = state.isLoading,
                            onVerifyCode = { code -> viewModel.onCodeEntered(code) }
                        )
                        ForgotPasswordStep.EnterNewPassword -> EnterNewPasswordStep(
                            isLoading = state.isLoading,
                            isFirstTime = isFirstTime,
                            onReset = { pwd, confirm -> viewModel.onResetWithCode(pwd, confirm) }
                        )
                    }
                }
            }

            if (state.isLoading) {
                LoadingOverlay(
                    isLoading = true,
                    message = if (state.step == ForgotPasswordStep.EnterEmail) "Enviando código..." else "Procesando..."
                )
            }
        }
    }
}

@Composable
private fun EnterEmailStep(
    isLoading: Boolean,
    isFirstTime: Boolean,
    onSendCode: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isFirstTime) 
                "Introduce tu correo electrónico institucional para solicitar tu código de primer ingreso." 
                else "Introduce tu correo electrónico para recibir un código de verificación.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !isLoading
        )
        Button(
            onClick = { onSendCode(email) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Solicitar Código")
        }
    }
}

@Composable
private fun EnterCodeStep(
    isLoading: Boolean,
    onVerifyCode: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Introduce el código de verificación que recibiste por correo.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Código de Verificación") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !isLoading
        )
        Button(
            onClick = { onVerifyCode(code) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Verificar Código")
        }
    }
}

@Composable
private fun EnterNewPasswordStep(
    isLoading: Boolean,
    isFirstTime: Boolean,
    onReset: (String, String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordConfirmation by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isFirstTime) 
                "Crea tu nueva contraseña para acceder al sistema." 
                else "Introduce tu nueva contraseña.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Nueva Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !isLoading
        )
        OutlinedTextField(
            value = passwordConfirmation,
            onValueChange = { passwordConfirmation = it },
            label = { Text("Confirmar Nueva Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onReset(password, passwordConfirmation) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isFirstTime) "Establecer Contraseña" else "Actualizar Contraseña")
        }
    }
}

    