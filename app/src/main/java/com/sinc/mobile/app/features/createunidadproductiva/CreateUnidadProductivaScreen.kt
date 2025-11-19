package com.sinc.mobile.app.features.createunidadproductiva

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sinc.mobile.app.features.createunidadproductiva.components.BottomNavBar
import com.sinc.mobile.app.features.createunidadproductiva.components.ProgressBar
import com.sinc.mobile.app.features.createunidadproductiva.components.Step1Ubicacion
import com.sinc.mobile.app.features.createunidadproductiva.components.Step2FormularioBasico
import com.sinc.mobile.app.features.createunidadproductiva.components.Step3FormularioOpcional
import com.sinc.mobile.app.ui.components.ConfirmationDialog
import com.sinc.mobile.ui.theme.colorBotonSiguiente
import com.sinc.mobile.ui.theme.md_theme_light_primary

@Composable
fun CreateUnidadProductivaScreen(
    viewModel: CreateUnidadProductivaViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStep = uiState.currentStep

    val titles = listOf(
        "Seleccionar ubicación", // Título corregido para el paso 1
        "Ubicación guardada",    // Título corregido para el paso 2
        "Datos básicos guardados" // Título corregido para el paso 3
    )

    val subtitles = listOf(
        "Buscando en el mapa", // Subtítulo corregido para el paso 1
        "Completando datos básicos", // Subtítulo corregido para el paso 2
        "Último paso, seleccione una opción" // Subtítulo para el paso 3 (queda igual)
    )

    ConfirmationDialog(
        showDialog = uiState.showExitDialog,
        title = "Salir del formulario",
        message = "Si sale, perderá todos los datos ingresados. ¿Está seguro de que desea salir?",
        onConfirm = {
            viewModel.onExitDialogDismiss()
            onNavigateBack()
        },
        onDismiss = viewModel::onExitDialogDismiss
    )

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            BottomNavBar(
                currentStep = currentStep,
                onPrevious = viewModel::onPreviousStep,
                onNext = viewModel::onNextStep
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(vertical = 16.dp)
        ) {
            // 1. Icono de Salir
            IconButton(
                onClick = viewModel::onExitRequest,
                modifier = Modifier.padding(horizontal = 4.dp) // Padding reducido para alinear
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Salir",
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp) // Padding general
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 2. Barra de Progreso
                ProgressBar(currentStep = currentStep)

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Títulos
                Text(
                    text = titles[currentStep - 1],
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = md_theme_light_primary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = subtitles[currentStep - 1],
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray)
                )

                // 4. Contenido del paso
                Box(modifier = Modifier.weight(1f)) {
                                    when (currentStep) {
                                        1 -> Step1Ubicacion(onNext = viewModel::onNextStep)
                                        2 -> Step2FormularioBasico(
                                            nombre = uiState.nombre,
                                            onNombreChange = viewModel::onNombreChange,
                                            rnspa = uiState.rnspa,
                                            onRnspaChange = viewModel::onRnspaChange,
                                            superficie = uiState.superficie,
                                            onSuperficieChange = viewModel::onSuperficieChange
                                        )
                                        3 -> Step3FormularioOpcional(
                                            selectedOption = uiState.condicionTenencia,
                                            options = viewModel.tenenciaOptions,
                                            onOptionSelected = viewModel::onCondicionTenenciaChange
                                        )
                                    }
                }
            }
        }
    }
}