package com.sinc.mobile.app.features.ventas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.ui.components.*
import com.sinc.mobile.ui.theme.*
import com.sinc.mobile.domain.model.Categoria
import com.sinc.mobile.domain.model.DeclaracionVenta
import com.sinc.mobile.domain.model.Especie
import com.sinc.mobile.domain.model.Raza
import com.sinc.mobile.domain.model.UnidadProductiva

@Composable
fun VentasScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistorial: () -> Unit,
    viewModel: VentasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            MinimalHeader(
                title = "Seguimiento de Ventas",
                onBackPress = onNavigateBack,
                actions = {
                    IconButton(onClick = onNavigateToHistorial) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Historial",
                            tint = Color.Black
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = SincBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            VentasList(
                declaraciones = uiState.declaracionesActivas,
                isLoading = uiState.isLoading,
                onRefresh = { viewModel.onSyncRequested() },
                onCancel = { id -> viewModel.onCancelDeclaracion(id) }
            )
        }

        if (uiState.isLoading && uiState.declaracionesActivas.isEmpty()) {
            LoadingOverlay(isLoading = true)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VentasList(
    declaraciones: List<DeclaracionVenta>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onCancel: (Int) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(refreshing = isLoading, onRefresh = onRefresh)

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        if (declaraciones.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No hay declaraciones en seguimiento.\nDeslice para actualizar.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(declaraciones) { declaracion ->
                    DeclaracionCard(declaracion, onCancel)
                }
            }
        }
        
        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun DeclaracionCard(
    declaracion: DeclaracionVenta,
    onCancel: (Int) -> Unit
) {
    val statusUi = com.sinc.mobile.app.ui.util.LogisticaUiMapper.getStatusUi(declaracion.estado)
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showCancelDialog) {
        com.sinc.mobile.app.ui.components.ConfirmationDialog(
            showDialog = true,
            title = "Cancelar Venta",
            message = "¿Está seguro de que desea cancelar esta declaración de venta? Esta acción no se puede deshacer.",
            onConfirm = {
                onCancel(declaracion.id)
                showCancelDialog = false
            },
            onDismiss = { showCancelDialog = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Cantidad y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${declaracion.cantidad} Animales",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stepper Simplificado
            if (statusUi.stepIndex >= 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pasos: Comprometido -> Recogido -> Planta -> Final
                    // Dibujamos 4 pasos
                    for (i in 0..3) {
                        val isCompleted = i <= statusUi.stepIndex
                        val isCurrent = i == statusUi.stepIndex
                        
                        // Dot
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 12.dp else 8.dp)
                                .background(
                                    color = if (isCompleted) statusUi.contentColor else Color.LightGray,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        
                        // Line (excepto el último)
                        if (i < 3) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(2.dp)
                                    .background(if (i < statusUi.stepIndex) statusUi.contentColor else Color.LightGray.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))
            
            // Detalles
            Text("Fecha Declaración: ${declaracion.fechaDeclaracion.take(10)}")
            
            declaracion.pesoAproximadoKg?.let { peso ->
                if (peso > 0) {
                    Text("Peso Vivo Aprox.: ${peso} Kg", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                }
            }
            
            declaracion.observaciones?.takeIf { it.isNotBlank() }?.let { obs ->
                ExpandableText(
                    text = obs,
                    prefix = "Obs:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
            
            // Motivo Rechazo (si existe)
            declaracion.motivoRechazo?.takeIf { it.isNotBlank() }?.let { motivo ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ExpandableText(
                        text = motivo,
                        prefix = "Motivo:",
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Botón Cancelar
            if (statusUi.canCancel) {
                Spacer(modifier = Modifier.height(16.dp))
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
        }
    }
}

