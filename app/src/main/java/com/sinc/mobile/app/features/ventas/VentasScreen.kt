package com.sinc.mobile.app.features.ventas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.ui.components.*
import com.sinc.mobile.app.ui.util.LogisticaUiMapper
import com.sinc.mobile.ui.theme.*
import com.sinc.mobile.domain.model.DeclaracionVenta

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

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VentasList(
    declaraciones: List<DeclaracionVenta>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onCancel: (Int) -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(refreshing = isLoading, onRefresh = onRefresh)
    var showSheet by remember { mutableStateOf(false) }
    var isCancelMode by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
        if (declaraciones.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No hay ventas activas en este ciclo.\nDeslice para actualizar.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            val totalAnimales = declaraciones.sumOf { it.cantidad }
            val rechazos = declaraciones.filter { it.motivoRechazo?.isNotBlank() == true }
            
            val maxStep = declaraciones.map { 
                LogisticaUiMapper.getStatusUi(it.estado).stepIndex 
            }.maxOrNull() ?: 0

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Surface(
                    onClick = { 
                        isCancelMode = false
                        showSheet = true 
                    },
                    color = Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Estado del Lote Actual",
                            style = MaterialTheme.typography.labelLarge,
                            color = SincPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "$totalAnimales Animales en Proceso",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Icon(
                                Icons.Default.Info, 
                                contentDescription = "Ver Detalle",
                                tint = SincPrimary.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))

                UnifiedVerticalStepper(
                    currentStep = maxStep,
                    declaraciones = declaraciones,
                    rechazos = rechazos,
                    onManageLot = {
                        isCancelMode = true
                        showSheet = true
                    }
                )
            }
        }
        
        if (showSheet) {
            LotDetailBottomSheet(
                declaraciones = declaraciones,
                isCancelMode = isCancelMode,
                onCancel = { id ->
                    onCancel(id)
                },
                onDismiss = { showSheet = false }
            )
        }

        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun UnifiedVerticalStepper(
    currentStep: Int,
    declaraciones: List<DeclaracionVenta>,
    rechazos: List<DeclaracionVenta>,
    onManageLot: () -> Unit
) {
    val steps = listOf(
        "Publicado" to "Declaración de intención de venta",
        "En Viaje" to "El camión ha recogido los animales",
        "En Planta" to "Los animales están en el matadero",
        "Finalizado" to "Ciclo completado con éxito"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, (title, desc) ->
            val isCompleted = index <= currentStep
            val isCurrent = index == currentStep
            val stepColor = if (isCompleted) SincPrimary else Color.LightGray.copy(alpha = 0.5f)

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(if (isCurrent) Color.White else stepColor, CircleShape)
                            .border(if (isCurrent) 4.dp else 0.dp, SincPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index < currentStep) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(10.dp), tint = Color.White)
                        }
                    }
                    
                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(if (index == 0) 140.dp else 100.dp)
                                .background(if (index < currentStep) SincPrimary else Color.LightGray.copy(alpha = 0.3f))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCompleted) Color.Black else Color.Gray
                    )
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    if (index == 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        if (currentStep <= 0) {
                            OutlinedButton(
                                onClick = onManageLot,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, SincPrimary),
                                contentPadding = PaddingValues(12.dp, 8.dp, 12.dp, 8.dp)
                            ) {
                                Icon(Icons.Default.Settings, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("GESTIONAR LOTE / CANCELAR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = SincGrayBackground,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "El lote ha sido procesado y no permite modificaciones.",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    if (index == 1 && rechazos.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFFC62828).copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                val cantRechazados = rechazos.sumOf { it.cantidad }
                                Text(
                                    "ATENCIÓN: $cantRechazados de ${declaraciones.sumOf { it.cantidad }} animales fueron rechazados en carga.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFC62828),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                rechazos.forEach { 
                                    Text("• ${it.motivoRechazo}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC62828))
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotDetailBottomSheet(
    declaraciones: List<DeclaracionVenta>,
    isCancelMode: Boolean,
    onCancel: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 0.dp, 0.dp, 32.dp)
        ) {
            Text(
                text = if (isCancelMode) "Gestionar Lote" else "Detalle del Lote",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(24.dp, 16.dp, 24.dp, 16.dp)
            )

            if (isCancelMode) {
                Text(
                    text = "Seleccione el animal que desea retirar de la declaración actual.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(24.dp, 0.dp, 24.dp, 16.dp)
                )
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(declaraciones) { dec ->
                    val statusUi = LogisticaUiMapper.getStatusUi(dec.estado)
                    
                    LotDetailItem(
                        dec = dec,
                        statusUi = statusUi,
                        isCancelMode = isCancelMode,
                        onCancel = onCancel
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(24.dp, 0.dp, 24.dp, 0.dp), 
                        color = Color.LightGray.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun LotDetailItem(
    dec: DeclaracionVenta,
    statusUi: com.sinc.mobile.app.ui.util.StatusUiModel,
    isCancelMode: Boolean,
    onCancel: (Int) -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }
    
    if (showConfirm) {
        ConfirmationDialog(
            showDialog = true,
            title = "Cancelar Registro",
            message = "¿Desea retirar estos ${dec.cantidad} animales del lote?",
            onConfirm = {
                onCancel(dec.id)
                showConfirm = false
            },
            onDismiss = { showConfirm = false }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp, 12.dp, 24.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${dec.cantidad} Animales",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Estado: ${statusUi.label}",
                style = MaterialTheme.typography.bodySmall,
                color = if (dec.estado.contains("rechazado")) Color(0xFFC62828) else Color.Gray
            )
        }

        if (isCancelMode && statusUi.canCancel) {
            IconButton(
                onClick = { showConfirm = true },
                modifier = Modifier.background(Color(0xFFFFEBEE), CircleShape)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Cancelar", tint = Color(0xFFC62828))
            }
        }
    }
}
