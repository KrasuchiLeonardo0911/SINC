package com.sinc.mobile.app.features.movimiento

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sinc.mobile.app.features.movimiento.components.UnidadSelectionStep
import com.sinc.mobile.app.navigation.Routes
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.ui.theme.SoftGray
import androidx.compose.foundation.layout.PaddingValues


import com.sinc.mobile.app.ui.components.CozyBottomNavBar
import com.sinc.mobile.app.ui.components.CozyBottomNavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionCampoScreen(
    modifier: Modifier = Modifier,
    viewModel: MovimientoViewModel = hiltViewModel(),
    navController: NavController
) {
    val state = viewModel.state.value

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        containerColor = SoftGray,
        topBar = {
            MinimalHeader(
                title = "Seleccionar Campo", // Changed title
                onBackPress = { navController.popBackStack() },
                modifier = Modifier
                    .statusBarsPadding()
            )
        },
        bottomBar = {
            CozyBottomNavBar(
                selectedRoute = CozyBottomNavRoutes.ADD,
                onItemSelected = { newRoute ->
                    when (newRoute) {
                        CozyBottomNavRoutes.HOME -> {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        }
                        CozyBottomNavRoutes.PROFILE -> {
                            // MainScreen handles showing the profile screen
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        }
                        // Other routes can be handled here if needed
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp) // Corrected padding
        ) {
            // --- 1. Selección de Campo ---
            item {
                UnidadSelectionStep(
                    unidades = state.unidades,
                    selectedUnidad = state.selectedUnidad,
                    onUnidadSelected = { unidad ->
                        // Navigate to the form screen with the selected ID
                        navController.navigate(Routes.createMovimientoFormRoute(unidad.id.toString()))
                    }
                )
            }
        }
    }
}