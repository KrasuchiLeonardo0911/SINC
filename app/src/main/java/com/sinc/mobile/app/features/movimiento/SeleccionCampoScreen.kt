package com.sinc.mobile.app.features.movimiento

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sinc.mobile.app.features.movimiento.components.UnidadSelectionStep
import com.sinc.mobile.app.navigation.Routes
import com.sinc.mobile.app.ui.components.MinimalHeader
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue


import com.sinc.mobile.app.ui.components.CozyBottomNavBar
import com.sinc.mobile.app.ui.components.CozyBottomNavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionCampoScreen(
    modifier: Modifier = Modifier,
    // Use MovimientoStepperViewModel as it manages the state for the overall movement process
    viewModel: MovimientoStepperViewModel = hiltViewModel(),
    navController: NavController
) {
    // Observe the UI state from MovimientoStepperViewModel
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MinimalHeader(
                title = "Seleccionar Campo",
                onBackPress = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .statusBarsPadding()
            )
        },
        bottomBar = {
            CozyBottomNavBar(
                selectedRoute = CozyBottomNavRoutes.ADD,
                onItemSelected = { newRoute ->
                    if (newRoute != CozyBottomNavRoutes.ADD) {
                        // Navigate to MainScreen and tell it which tab to open
                        navController.navigate(Routes.HOME + "?startRoute=${newRoute}") {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            // --- 1. Selección de Campo ---
            item {
                UnidadSelectionStep(
                    unidades = uiState.unidades, // Use units from MovimientoStepperViewModel
                    selectedUnidad = uiState.selectedUnidad, // Selected unit should come from ViewModel
                    onUnidadSelected = { unidad ->
                        // This screen just selects. The ViewModel's selectedUnidad will be updated via navigation state.
                        // Navigating to the form will then use this selectedUnidad.
                        navController.navigate(Routes.createMovimientoFormRoute(unidad.id.toString()))
                    }
                )
            }
        }
    }
}