package com.sinc.mobile.app.features.movimiento

import com.sinc.mobile.app.ui.components.MinimalHeader


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.R
import com.sinc.mobile.app.features.movimiento.components.*
import com.sinc.mobile.app.ui.components.InfoDialog
import com.sinc.mobile.ui.theme.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientoScreen(
    modifier: Modifier = Modifier,
    viewModel: MovimientoViewModel = hiltViewModel(),
    navController: NavController
) {
    val state = viewModel.state.value
    val syncState = viewModel.syncManager.syncState.value
    var showInfoDialog by remember { mutableStateOf(false) }

    val isFormValid = viewModel.formManager?.formState?.value?.isFormValid ?: false

    InfoDialog(
        showDialog = showInfoDialog,
        onDismiss = { showInfoDialog = false },
        title = "Instrucciones del Formulario",
        message = "Para registrar un movimiento, selecciona la especie y el motivo. La categoría y la raza son opcionales. Luego, indica la cantidad de animales."
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        containerColor = SoftGray,
        topBar = {
            MinimalHeader(
                title = "Carga de Stock",
                onBackPress = { navController.popBackStack() },
                modifier = Modifier
                    .statusBarsPadding()
            )
        },
        bottomBar = {
            if (state.selectedUnidad != null && !state.isUnidadSelectedLoading) {
                MovimientoBottomBar(
                    onSave = viewModel::saveMovement,
                    isSaving = state.isSaving,
                    enabled = isFormValid && !state.isSaving
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(6
                .dp),
            // Removed verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. Selección de Campo ---
            item {
                UnidadSelectionStep(
                    unidades = state.unidades,
                    selectedUnidad = state.selectedUnidad,
                    onUnidadSelected = viewModel::onUnidadSelected
                )
                Spacer(Modifier.height(16.dp)) // Add spacer after UnidadSelectionStep
            }

            // --- Content ---
            if (state.selectedUnidad == null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 128.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ilustracion_agregar_movimiento_screen),
                            contentDescription = "Ilustración de Stock",
                            modifier = Modifier
                                .size(180.dp)
                                .clip(CircleShape)
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "No hay movimientos pendientes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = CozyTextMain
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Selecciona un campo arriba para registrar nuevas entradas o salidas de stock.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else if (state.isUnidadSelectedLoading) {
                item {
                    MovimientoSkeletonLoader()
                }
            } else {
                // --- Form Title with Tooltip ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Completar los Datos", // Changed text
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = CozyTextMain,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showInfoDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Instrucciones",
                                tint = CozyTextSecondary
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp)) // Reduced spacer height
                }

                // --- Form Card ---
                viewModel.formManager?.let { fm ->
                    item {
                        MovimientoForm(
                            formState = fm.formState.value,
                            onEspecieSelected = fm::onEspecieSelected,
                            onCategoriaSelected = fm::onCategoriaSelected,
                            onRazaSelected = fm::onRazaSelected,
                            onMotivoSelected = fm::onMotivoSelected,
                            onCantidadChanged = fm::onCantidadChanged,
                        )
                    }
                }
            }
        }
    }
}