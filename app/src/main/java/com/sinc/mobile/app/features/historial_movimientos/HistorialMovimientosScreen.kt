package com.sinc.mobile.app.features.historial_movimientos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.app.ui.components.FullscreenLoader
import com.sinc.mobile.domain.model.MovimientoHistorial
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

import com.sinc.mobile.ui.theme.SincBackground
import com.sinc.mobile.ui.theme.SincGrayBackground

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HistorialMovimientosScreen(
    viewModel: HistorialMovimientosViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToResumen: (Int, Int) -> Unit
) {
    val state by viewModel.state.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading,
        onRefresh = { viewModel.syncMovimientos() }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        containerColor = SincBackground
    ) { paddingValues ->
        if (state.isInitialLoad) {
            FullscreenLoader(
                message = "Cargando historial...",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. Lista de Movimientos (Scrollable)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 48.dp + 64.dp + 24.dp,
                            bottom = 32.dp
                        )
                    ) {
                        if (state.filteredMovimientos.isEmpty() && !state.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillParentMaxSize()
                                        .padding(bottom = 150.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No hay movimientos guardados en este mes.",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        itemsIndexed(state.filteredMovimientos) { index, movimiento ->
                            CompactMovimientoRow(movimiento = movimiento)
                            
                            if (index < state.filteredMovimientos.size - 1) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = Color.LightGray.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    PullRefreshIndicator(
                        refreshing = state.isLoading,
                        state = pullRefreshState,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 48.dp + 64.dp),
                        backgroundColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }

                // 2. Cabecero y Selector Fijos (Top)
                Column(modifier = Modifier.align(Alignment.TopCenter)) {
                    MinimalHeader(
                        title = "Historial de Movimientos",
                        onBackPress = onBack,
                        actions = {
                            IconButton(onClick = { 
                                onNavigateToResumen(state.selectedDate.monthValue, state.selectedDate.year) 
                            }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Resumen Mensual",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                    
                    Surface(
                        color = SincBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            MonthSelector(
                                currentDate = state.selectedDate,
                                onPrevious = { viewModel.previousMonth() },
                                onNext = { viewModel.nextMonth() }
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthSelector(
    currentDate: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mes Anterior")
        }

        Text(
            text = currentDate.format(formatter).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Mes Siguiente")
        }
    }
}

@Composable
fun CompactMovimientoRow(movimiento: MovimientoHistorial) {
    val isAlta = movimiento.tipoMovimiento.equals("alta", ignoreCase = true)
    val color = if (isAlta) Color(0xFF2E7D32) else Color(0xFFC62828)
    val icon = if (isAlta) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = movimiento.motivo,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${movimiento.especie} (${movimiento.raza}) - ${movimiento.categoria}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            if (movimiento.destinoTraslado != null) {
                Text(
                    text = "Destino: ${movimiento.destinoTraslado}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if (isAlta) "+" else "-"}${movimiento.cantidad}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = movimiento.fechaRegistro.format(DateTimeFormatter.ofPattern("dd MMM")),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
