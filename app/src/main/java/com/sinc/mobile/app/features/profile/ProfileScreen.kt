package com.sinc.mobile.app.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.features.profile.components.ProfileInfoCard
import com.sinc.mobile.app.features.profile.components.InfoRow
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.ui.theme.SincBackground
import android.content.Intent
import android.net.Uri

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTerms: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            MinimalHeader(
                title = "Mi Perfil",
                onBackPress = onNavigateBack
            )
        },
        containerColor = SincBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.user != null -> {
                    val user = uiState.user!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProfileInfoCard(title = "Datos de la Cuenta") {
                            InfoRow(label = "Nombre", value = user.name)
                            InfoRow(label = "Email", value = user.email)
                        }

                        user.productor?.let { productor ->
                            ProfileInfoCard(title = "Datos del Productor") {
                                InfoRow(label = "Nombre Productor", value = productor.nombre)
                                productor.dni?.let { InfoRow(label = "DNI", value = it) }
                                productor.cuil?.let { InfoRow(label = "CUIL", value = it) }
                                productor.telefono?.let { InfoRow(label = "Teléfono", value = it) }
                                productor.direccion?.let { InfoRow(label = "Dirección", value = it) }
                                productor.paraje?.let { InfoRow(label = "Paraje", value = it) }
                                productor.fechaNacimiento?.let { InfoRow(label = "Fecha de Nacimiento", value = it) }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Si quiere actualizar los datos, deberá hacerlo desde nuestra web.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val context = LocalContext.current
                            val websiteUrl = "https://sicsurmisiones.online"
                            Text(
                                text = websiteUrl,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                ),
                                modifier = Modifier
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
                                        context.startActivity(intent)
                                    }
                                    .padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
