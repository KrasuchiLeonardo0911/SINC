package com.sinc.mobile.app.features.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.ui.theme.SincBackground

@Composable
fun TermsOfServiceScreen(
    onBackPress: () -> Unit
) {
    Scaffold(
        topBar = {
            MinimalHeader(
                title = "Términos de Servicio",
                onBackPress = onBackPress
            )
        },
        containerColor = SincBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Términos y Condiciones de Uso",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = """
                    Este es un espacio reservado para los Términos y Condiciones de Servicio de la aplicación SINC.
                    
                    Aquí se detallarán las responsabilidades del usuario, el uso de la información, y las normas de conducta dentro de la plataforma.
                    
                    Próximamente se integrará el contenido legal completo.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
