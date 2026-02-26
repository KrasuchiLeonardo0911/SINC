package com.sinc.mobile.app.features.ventas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.HorizontalDivider
import com.sinc.mobile.app.ui.components.ExpandableText
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.ui.theme.SincBackground
import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.model.DeclaracionVenta
import com.sinc.mobile.app.ui.util.LogisticaUiMapper
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
                onBackPress = onNavigateBack
            )
        },
        containerColor = SincBackground
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
                catalogos = uiState.catalogos,
                onClose = { viewModel.seleccionarDeclaracion(null) },
                onCancel = { id -> 
                    viewModel.onCancelDeclaracion(id)
                    viewModel.seleccionarDeclaracion(null)
                }
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
    val statusUi = LogisticaUiMapper.getStatusUi(declaracion.estado)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
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
                // Indicador de Estado
                Surface(
                    color = statusUi.containerColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = statusUi.label.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusUi.contentColor
                    )
                }
            }
        }
    }
}

@Composable
fun DetalleVentaSheet(
    declaracion: DeclaracionVenta,
    catalogos: Catalogos?,
    onClose: () -> Unit,
    onCancel: (Int) -> Unit
) {
    val statusUi = LogisticaUiMapper.getStatusUi(declaracion.estado)
    val especieNombre = catalogos?.especies?.find { it.id == declaracion.especieId }?.nombre ?: "ID: ${declaracion.especieId}"
    val razaNombre = catalogos?.razas?.find { it.id == declaracion.razaId }?.nombre ?: "ID: ${declaracion.razaId}"
    val categoriaNombre = catalogos?.categorias?.find { it.id == declaracion.categoriaAnimalId }?.nombre ?: "ID: ${declaracion.categoriaAnimalId}"
    
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showCancelDialog) {
        com.sinc.mobile.app.ui.components.ConfirmationDialog(
            showDialog = true,
            title = "Cancelar Venta",
            message = "¿Está seguro de que desea cancelar esta declaración? Esta acción no se puede deshacer.",
            onConfirm = {
                onCancel(declaracion.id)
                showCancelDialog = false
            },
            onDismiss = { showCancelDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        Text(
            text = "Detalle de Venta",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        // Estado (Debajo del título)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Estado Actual:", fontWeight = FontWeight.SemiBold)
            Surface(
                color = statusUi.containerColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    statusUi.icon?.let {
                        Icon(it, contentDescription = null, tint = statusUi.contentColor, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = statusUi.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusUi.contentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        HorizontalDivider()
        
        // Línea de Tiempo (Timeline)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Seguimiento Logístico", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            
            TimelineRow("Declarado", declaracion.fechaDeclaracion, isCompleted = true)
            TimelineRow("Recogido", declaracion.fechaRecogida, isCompleted = declaracion.fechaRecogida != null)
            TimelineRow("En Planta", declaracion.fechaMatadero, isCompleted = declaracion.fechaMatadero != null)
            TimelineRow("Entregado", declaracion.fechaEntrega, isCompleted = declaracion.fechaEntrega != null)
        }
        
        // Motivo de Rechazo (Alerta)
        declaracion.motivoRechazo?.let { motivo ->
            if (motivo.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ExpandableText(
                        text = motivo,
                        prefix = "Motivo:",
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        
        // Datos del Animal
        DetalleRow("Especie", especieNombre)
        DetalleRow("Raza", razaNombre)
        DetalleRow("Categoría", categoriaNombre)
        DetalleRow("Cantidad", "${declaracion.cantidad} Animales")
        
        declaracion.pesoAproximadoKg?.let { 
            DetalleRow("Peso Vivo Aprox.", "$it Kg") 
        }

        declaracion.observaciones?.let { obs ->
            if (obs.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                ExpandableText(
                    text = obs,
                    prefix = "Obs:",
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Botones de Acción
        if (statusUi.canCancel) {
            OutlinedButton(
                onClick = { showCancelDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancelar Venta")
            }
        }
        
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
fun TimelineRow(label: String, date: String?, isCompleted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(
                    color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.LightGray,
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        
        // Text
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCompleted) Color.Black else Color.Gray,
                fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Date
        if (date != null) {
            Text(
                text = date.take(10), // Simple format YYYY-MM-DD
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )
        } else {
            Text(
                text = "--",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun DetalleRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(text = label, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(2f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
