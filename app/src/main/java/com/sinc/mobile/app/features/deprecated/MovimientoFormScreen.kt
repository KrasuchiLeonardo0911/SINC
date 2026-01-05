/*
package com.sinc.mobile.app.features.movimiento

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sinc.mobile.app.features.movimiento.components.MovimientoBottomBar
import com.sinc.mobile.app.features.movimiento.components.MovimientoForm
import com.sinc.mobile.app.features.movimiento.components.MovimientoSkeletonLoader
import com.sinc.mobile.app.ui.components.InfoDialog
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.app.ui.components.shimmerBrush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientoFormScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    unidadId: String,
    viewModel: MovimientoViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    var showInfoDialog by remember { mutableStateOf(false) }

    val isFormValid = viewModel.formManager?.formState?.value?.isFormValid ?: false
    val snackbarHostState = remember { SnackbarHostState() }

    // Trigger the loading of data for the selected unidad
    LaunchedEffect(key1 = unidadId, key2 = state.unidades) {
        val selectedUnidadObject = state.unidades.find { it.id.toString() == unidadId }
        if (selectedUnidadObject != null) {
            viewModel.onUnidadSelected(selectedUnidadObject)
        }
    }

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MinimalHeader(
                title = "Carga de Stock",
                onBackPress = { navController.popBackStack() },
                modifier = Modifier
                    .statusBarsPadding(),
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Instrucciones",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
        ) {
            if (state.isUnidadSelectedLoading) {
                item {
                    // Skeleton for the entire screen content
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Skeleton for Title
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(24.dp)
                                .background(shimmerBrush(showShimmer = state.showShimmer))
                        )
                        // Skeleton for Form
                        MovimientoSkeletonLoader(showShimmer = state.showShimmer)
                    }
                }
            } else {
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
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }
        }
    }
}
*/