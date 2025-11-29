package com.sinc.mobile.app.features.createunidadproductiva

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.features.createunidadproductiva.components.BottomNavBar
import com.sinc.mobile.app.features.createunidadproductiva.components.ProgressBar
import com.sinc.mobile.app.features.createunidadproductiva.components.Step1Ubicacion
import com.sinc.mobile.app.features.createunidadproductiva.components.Step2FormularioBasico
import com.sinc.mobile.app.features.createunidadproductiva.components.Step3FormularioOpcional
import com.sinc.mobile.app.ui.components.ConfirmationDialog
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.navigationBarsPadding
import com.sinc.mobile.ui.theme.md_theme_light_primary
import androidx.compose.material3.ButtonDefaults
import com.sinc.mobile.ui.theme.colorBotonSiguiente

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateUnidadProductivaScreen(
    viewModel: CreateUnidadProductivaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStep = uiState.currentStep

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.onPermissionResult(isGranted)
        }
    )

    val bottomSheetState = rememberModalBottomSheetState()

    if (uiState.showPermissionBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onPermissionBottomSheetDismissed,
            sheetState = bottomSheetState,
            containerColor = Color.White,
            scrimColor = Color.Black.copy(alpha = 0.6f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Permiso de Ubicación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorBotonSiguiente // Changed to green
                )
                Text(
                    buildAnnotatedString {
                        append("Para registrar la ubicación de su campo, necesitamos acceder a la ubicación de su dispositivo. Esta se utilizará ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("únicamente para marcar su posición en el mapa")
                        }
                        append(" cuando presione el botón ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("\"Usar mi ubicación actual\"")
                        }
                        append(".")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = {


                        viewModel.onPermissionBottomSheetDismissed()
                        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colorBotonSiguiente)
                ) {
                    Text("Aceptar")
                }
            }
        }
    }

    val titles = listOf(
        "Seleccionar ubicación",
        "Ubicación guardada",
        "Datos básicos guardados"
    )

    val subtitles = listOf(
        "Buscando en el mapa",
        "Completando datos básicos",
        "Último paso, seleccione una opción"
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
                modifier = Modifier.navigationBarsPadding(),
                currentStep = currentStep,
                onPrevious = viewModel::onPreviousStep,
                onNext = viewModel::onNextStep
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(vertical = 16.dp)
        ) {
            IconButton(
                onClick = viewModel::onExitRequest,
                modifier = Modifier.padding(horizontal = 4.dp)
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
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                ProgressBar(currentStep = currentStep)

                Spacer(modifier = Modifier.height(24.dp))

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

                Box(modifier = Modifier.weight(1f)) {
                    when (currentStep) {
                                                                1 -> Step1Ubicacion(
                                                                    isMapVisible = uiState.isMapVisible,
                                                                    mapMode = uiState.mapMode,
                                                                    selectedLocation = uiState.selectedLocation,
                                                                    locationError = uiState.locationError,
                                                                    onUseCurrentLocation = viewModel::onUseCurrentLocationClicked,
                                                                    onSearchOnMap = viewModel::onSearchOnMapClicked,
                                                                    onMapDismissed = viewModel::onMapDismissed,
                                                                    animateToLocation = uiState.animateToLocation,
                                                                    onAnimationCompleted = viewModel::onMapAnimationCompleted,
                                                                    onConfirmLocation = viewModel::onMapLocationSelected,
                                                                    isFetchingLocation = uiState.isFetchingLocation,
                                                                    municipios = uiState.municipios,
                                                                    selectedMunicipio = uiState.selectedMunicipio,
                                                                    onMunicipioSelected = viewModel::onMunicipioSelected
                                                                )
                                                                                                                                                        2 -> Step2FormularioBasico(
                                                                                                                                                            nombre = uiState.nombre,                            onNombreChange = viewModel::onNombreChange,
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