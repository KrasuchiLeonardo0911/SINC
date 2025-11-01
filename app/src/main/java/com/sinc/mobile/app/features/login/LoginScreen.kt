package com.sinc.mobile.app.features.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.R

@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val state = viewModel.state.value
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logoovinos),
                contentDescription = "Logo de la App",
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.onLoginClick(email, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Text("Login")
            }

            if (state.isLoading) {
                CircularProgressIndicator()
            }

            state.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            if (state.loginSuccess) {
                Text(text = "¡Login Exitoso!", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
