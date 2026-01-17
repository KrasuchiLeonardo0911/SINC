package com.sinc.mobile.app.features.ventas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.HorizontalDivider
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.domain.model.DeclaracionVenta
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HistorialVentasScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistorialVentasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = { viewModel.onSyncRequested() }
    )

    Scaffold(
        topBar = {
            MinimalHeader(
                title = "Historial de Ventas",
                onBackPress = onNavigateBack,
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Filtro de Mes
            MonthSelector(
                currentDate = uiState.filtroMes,
                onPrevious = { viewModel.mesAnterior() },
                onNext = { viewModel.mesSiguiente() }
            )

            // Lista con Pull Refresh
            Box(modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)) {
                
                if (uiState.declaracionesFiltradas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay ventas registradas en este mes.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.declaracionesFiltradas) { item ->
                            HistorialVentaItem(
                                declaracion = item,
                                onClick = { viewModel.seleccionarDeclaracion(item) }
                            )
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = uiState.isLoading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }

    // Modal de Detalle
    if (uiState.declaracionSeleccionada != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.seleccionarDeclaracion(null) },
            containerColor = Color.White
        ) {
            DetalleVentaSheet(
                declaracion = uiState.declaracionSeleccionada!!,
                onClose = { viewModel.seleccionarDeclaracion(null) }
            )
        }
    }
}

@Composable
fun MonthSelector(
    currentDate: java.time.LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
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
fun HistorialVentaItem(
    declaracion: DeclaracionVenta,
    onClick: () -> Unit
) {
    val statusColor = when (declaracion.estado.lowercase()) {
        "aprobado", "completado" -> Color(0xFF2E7D32) // Verde
        "cancelado", "rechazado" -> Color(0xFFC62828) // Rojo
        else -> Color(0xFFF57C00) // Naranja (Pendiente)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${declaracion.cantidad} Animales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = declaracion.fechaDeclaracion.take(10),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Indicador de Estado (Punto de color + Texto)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusColor, RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = declaracion.estado.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
fun DetalleVentaSheet(declaracion: DeclaracionVenta, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Detalle de Venta",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()
        
        DetalleRow("ID Venta", "#${declaracion.id}")
        DetalleRow("Fecha Declarada", declaracion.fechaDeclaracion.take(10))
        DetalleRow("Estado Actual", declaracion.estado.uppercase())
        DetalleRow("Cantidad", "${declaracion.cantidad} Animales")
        
        declaracion.pesoAproximadoKg?.let { 
            DetalleRow("Peso Aproximado", "$it Kg") 
        }

        declaracion.observaciones?.let { obs ->
            if (obs.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Observaciones:", fontWeight = FontWeight.Bold)
                Text(obs, color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cerrar")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DetalleRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray)
        Text(text = value, fontWeight = FontWeight.SemiBold)
    }
}
