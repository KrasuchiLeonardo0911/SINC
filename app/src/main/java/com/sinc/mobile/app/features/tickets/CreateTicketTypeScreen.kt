package com.sinc.mobile.app.features.tickets

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.ui.components.MinimalHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketTypeScreen(
    onNavigateBack: () -> Unit,
    onTypeSelected: (String) -> Unit
) {
    Scaffold(
        topBar = {
            MinimalHeader(
                title = "Nueva Consulta",
                onBackPress = onNavigateBack,
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "¿Sobre qué tema es tu consulta?",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = { onTypeSelected("problema_tecnico") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text("Problema Técnico")
            }

            Button(
                onClick = { onTypeSelected("consulta_negocio") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text("Consulta de Negocio")
            }
        }
    }
}
