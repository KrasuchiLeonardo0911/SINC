package com.sinc.mobile.app.features.movimiento

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.features.movimiento.components.ActionSelectionStep
import com.sinc.mobile.app.features.movimiento.components.MovimientoForm
import com.sinc.mobile.app.features.movimiento.components.MovimientoItemCard
import com.sinc.mobile.app.features.movimiento.components.UnidadSelectionStep
import com.sinc.mobile.ui.theme.*

@Composable
fun MovimientoScreen(
    modifier: Modifier = Modifier,
    viewModel: MovimientoViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val syncState = viewModel.syncManager.syncState.value

    Column(modifier = modifier.fillMaxSize().background(colorFondo)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. Selección de Campo ---
            item {
                Text(
                    "Seleccione campo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorTextoPrincipal.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )
                UnidadSelectionStep(
                    unidades = state.unidades,
                    selectedUnidad = state.selectedUnidad,
                    isDropdownExpanded = state.isDropdownExpanded,
                    onExpandedChange = viewModel::onDropdownExpandedChange,
                    onUnidadSelected = viewModel::onUnidadSelected
                )
            }

            // --- 2. Panel de Acciones ---
            if (state.selectedUnidad != null) {
                item {
                    ActionSelectionStep(
                        onActionSelected = viewModel::onActionSelected,
                        selectedAction = state.selectedAction
                    )
                }
            }

            // --- 3. Formulario de Registro (Aparece al seleccionar una acción) ---
            item {
                val currentAction = state.selectedAction
                AnimatedVisibility(
                    visible = currentAction != null,
                    enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { -40 }),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    viewModel.formManager?.let { formManager ->
                        if (currentAction != null) {
                            MovimientoForm(
                                formManager = formManager,
                                selectedAction = currentAction,
                                onSave = viewModel::saveMovement,
                                onDismiss = { viewModel.onActionSelected("") }, // Empty action dismisses
                                isSaving = state.isSaving,
                                saveError = state.saveError
                            )
                        }
                    }
                }
            }

            // --- 4. Lista de Movimientos Pendientes ---
            if (syncState.movimientosAgrupados.isNotEmpty()) {
                item {
                    Text(
                        "Pendientes de Sincronizar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colorTextoPrincipal.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
                    )
                }
                items(syncState.movimientosAgrupados) { movimiento ->
                    MovimientoItemCard(
                        movimiento = movimiento,
                        catalogos = state.catalogos,
                        unidades = state.unidades,
                        onDelete = viewModel.syncManager::deleteMovimientoGroup
                    )
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = viewModel.syncManager::syncMovements,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !syncState.isSyncing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorSuperficie,
                            contentColor = colorTextoPrincipal
                        ),
                        border = BorderStroke(1.dp, colorBorde),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        if (syncState.isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Sincronizar")
                            Spacer(Modifier.width(8.dp))
                            Text("Sincronizar Todo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}