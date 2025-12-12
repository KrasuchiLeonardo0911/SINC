package com.sinc.mobile.app.features.movimiento

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.R // <- Movida al lugar correcto
import com.sinc.mobile.app.features.movimiento.components.*
import com.sinc.mobile.ui.theme.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.sinc.mobile.app.ui.components.CozyBottomNavBar
import com.sinc.mobile.app.ui.components.CozyBottomNavRoutes

@Composable
fun MovimientoScreen(
    modifier: Modifier = Modifier,
    viewModel: MovimientoViewModel = hiltViewModel(),
    navController: NavController
) {
    val state = viewModel.state.value
    val syncState = viewModel.syncManager.syncState.value
    var currentRoute by remember { mutableStateOf(CozyBottomNavRoutes.ADD) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        containerColor = SoftGray, // Cambio a SoftGray
        bottomBar = {
            CozyBottomNavBar(
                selectedRoute = currentRoute,
                onItemSelected = { newRoute ->
                    if (newRoute != CozyBottomNavRoutes.ADD) {
                        navController.navigate(newRoute) {
                            // Basic navigation, can be improved with a proper navigator
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 0. Título de la Pantalla ---
            item {
                Text(
                    text = "Movimientos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = CozyTextMain,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
            }

            // --- 1. Selección de Campo ---
            item {
                UnidadSelectionStep(
                    unidades = state.unidades,
                    selectedUnidad = state.selectedUnidad,
                    onUnidadSelected = viewModel::onUnidadSelected
                )
            }

            // --- A. Estado Vacío (cuando no hay campo seleccionado) ---
            if (state.selectedUnidad == null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 128.dp) // Ajustado el padding superior a 128.dp
                            .padding(horizontal = 16.dp),
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
            } else {
                // ... (el resto de la lógica de la pantalla se mantiene igual)
            }
        }
    }
}