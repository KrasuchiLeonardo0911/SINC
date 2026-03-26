package com.sinc.mobile.app.features.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.features.stock.components.PieChart
import com.sinc.mobile.app.features.stock.components.CampoSelector
import com.sinc.mobile.app.features.stock.components.UpSelectorBottomSheet
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.app.ui.components.FullscreenLoader
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigateToVentas: () -> Unit,
    onNavigateToMovimientoForm: (Int, Int, Int, Int) -> Unit, // speciesId, breedId, catId, upId
    viewModel: StockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val processedStock = uiState.processedStock
    var showUpSelector by remember { mutableStateOf(false) }
    
    // Local state to manage which species is expanded
    var expandedSpecies by remember { mutableStateOf<String?>(null) }

    // Quick sale state
    var showSaleSheet by remember { mutableStateOf(false) }
    var selectedItemForSale by remember { mutableStateOf<DesgloseItem.Full?>(null) }
    var selectedSpeciesNameForSale by remember { mutableStateOf("") }
    var pesoVenta by remember { mutableStateOf("") }
    var observacionesVenta by remember { mutableStateOf("") }

    LaunchedEffect(uiState.saleSuccess, uiState.saleError) {
        if (uiState.saleSuccess != null || uiState.saleError != null) {
            showSaleSheet = false
            // The Snackbar/Dialog is handled below
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().navigationBarsPadding(),
        containerColor = Color.White,
    ) { paddingValues ->
        if (uiState.isInitialLoad) {
            FullscreenLoader(
                message = "Cargando su stock...",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            val pullRefreshState = rememberPullRefreshState(refreshing = uiState.isLoading, onRefresh = viewModel::refresh)

            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState),
                    contentPadding = PaddingValues(
                        start = 0.dp,
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 48.dp + 32.dp, 
                        end = 0.dp,
                        bottom = 24.dp
                    ), 
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Selector de Campo
                    item {
                        val selectedUnidad = uiState.unidadesProductivas.find { it.id == uiState.selectedUnidadId }
                        CampoSelector(
                            selectedUnidadName = selectedUnidad?.nombre ?: "Todos los campos",
                            onClick = { showUpSelector = true }
                        )
                        Spacer(modifier = Modifier.height(64.dp))
                    }

                    if (processedStock != null && processedStock.stockTotalGeneral > 0) {
                        // Sección de Stock Total (Gráfico)
                        item {
                            TotalStockHeader(stock = processedStock)
                        }

                        // Lista de Especies con Detalle Integrado
                        items(processedStock.allSpecies.size) { index ->
                            val species = processedStock.allSpecies[index]
                            val isExpanded = expandedSpecies == species.nombre
                            
                            SpeciesExpandableCard(
                                species = species,
                                isExpanded = isExpanded,
                                onToggle = {
                                    expandedSpecies = if (isExpanded) null else species.nombre
                                },
                                onAdjust = { item ->
                                    uiState.selectedUnidadId?.let { upId ->
                                        onNavigateToMovimientoForm(item.especieId, item.razaId, item.categoriaId, upId)
                                    }
                                },
                                onSell = { item ->
                                    selectedItemForSale = item
                                    selectedSpeciesNameForSale = species.nombre
                                    pesoVenta = ""
                                    observacionesVenta = ""
                                    showSaleSheet = true
                                }
                            )

                            if (index < processedStock.allSpecies.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 1.dp
                                )
                            }
                        }
                    } else if (!uiState.isLoading) {
                        item {
                            EmptyStockState(
                                message = if (uiState.selectedUnidadId != null) 
                                    "Este campo no tiene stock registrado." 
                                else "Aún no tienes stock registrado en tus campos."
                            )
                        }
                    }
                }

                // Header Fijo Superior
                MinimalHeader(
                    title = "Mi Stock",
                    onBackPress = onBack,
                    modifier = Modifier.align(Alignment.TopCenter),
                    actions = {
                        IconButton(onClick = onNavigateToVentas) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = "Seguimiento",
                                tint = Color.Black
                            )
                        }
                    }
                )

                PullRefreshIndicator(
                    refreshing = uiState.isLoading,
                    state = pullRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 48.dp),
                    backgroundColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // Modal de Venta Rápida
    if (showSaleSheet && selectedItemForSale != null) {
        ModalBottomSheet(
            onDismissRequest = { showSaleSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Registrar Venta",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${selectedItemForSale?.categoria} - ${selectedItemForSale?.raza}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (uiState.selectedUnidadId == null) {
                    Text(
                        text = "Para vender, debe seleccionar un campo arriba primero.",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                } else if (!uiState.isLogisticsOpen) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "El periodo de ventas ha cerrado, espere hasta el siguiente ciclo.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Cantidad a declarar: 1 animal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = pesoVenta,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) pesoVenta = it },
                        label = { Text("Peso Vivo Aproximado (Kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = observacionesVenta,
                        onValueChange = { observacionesVenta = it },
                        label = { Text("Observaciones (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.onSellAnimal(
                                especieNombre = selectedSpeciesNameForSale,
                                categoriaNombre = selectedItemForSale!!.categoria,
                                razaNombre = selectedItemForSale!!.raza,
                                cantidad = 1,
                                peso = pesoVenta.toFloatOrNull(),
                                observaciones = observacionesVenta,
                                unidadId = uiState.selectedUnidadId!!
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        enabled = !uiState.isSelling
                    ) {
                        if (uiState.isSelling) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Confirmar Venta")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Success/Error Dialogs
    if (uiState.saleSuccess != null || uiState.saleError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSaleMessages() },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSaleMessages() }) {
                    Text("Entendido")
                }
            },
            title = { Text(if (uiState.saleSuccess != null) "Éxito" else "Atención") },
            text = { Text(uiState.saleSuccess ?: uiState.saleError ?: "") },
            containerColor = Color.White
        )
    }

    if (showUpSelector) {
        UpSelectorBottomSheet(
            unidades = uiState.unidadesProductivas,
            selectedUnidadId = uiState.selectedUnidadId,
            onSelectUnidad = { 
                viewModel.selectUnidad(it)
                showUpSelector = false
            },
            onDismiss = { showUpSelector = false }
        )
    }
}

@Composable
private fun TotalStockHeader(stock: ProcessedStock) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 8.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .shadow(elevation = 6.dp, shape = CircleShape, ambientColor = Color.Black.copy(alpha = 0.3f))
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (stock.speciesDistribution.isNotEmpty()) {
                PieChart(
                    data = stock.speciesDistribution,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 45f
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stock.stockTotalGeneral.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    ),
                    color = Color(0xFF191C1E)
                )
                Text(
                    text = "Animales",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
private fun SpeciesExpandableCard(
    species: ProcessedEspecieStock,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAdjust: (DesgloseItem.Full) -> Unit,
    onSell: (DesgloseItem.Full) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        // Cabecera de Especie (Clickable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(species.color)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = species.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191C1E)
                    )
                    Text(
                        text = "${species.stockTotal} animales en total",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            val rotationAngle by animateFloatAsState(
                targetValue = if (isExpanded) 180f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "ArrowRotation"
            )

            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(rotationAngle)
            )
        }

        // Contenido Expandido (Tabla de Detalle)
        androidx.compose.animation.AnimatedVisibility(
            visible = isExpanded,
            enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)) + androidx.compose.animation.expandVertically(
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            ),
            exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300)) + androidx.compose.animation.shrinkVertically(
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 300, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
        ) {
            Column {
                StockDetailTable(
                    desglose = species.desglose,
                    onAdjust = onAdjust,
                    onSell = onSell
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StockDetailTable(
    desglose: List<DesgloseItem.Full>,
    onAdjust: (DesgloseItem.Full) -> Unit,
    onSell: (DesgloseItem.Full) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        // Encabezado de Tabla
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F7), RoundedCornerShape(4.dp))
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ANIMAL / DETALLE",
                modifier = Modifier.weight(1.5f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Text(
                text = "ACCIONES",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Text(
                text = "STOCK",
                modifier = Modifier.weight(0.5f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                textAlign = TextAlign.End
            )
        }

        // Filas de Datos
        desglose.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info Animal
                Column(modifier = Modifier.weight(1.5f)) {
                    Text(
                        text = item.categoria,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.raza,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Acciones
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onAdjust(item) }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Ajustar",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { onSell(item) }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Vender",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Cantidad
                Text(
                    text = item.quantity.toString(),
                    modifier = Modifier.weight(0.5f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
        }
    }
}

@Composable
private fun EmptyStockState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().height(400.dp).padding(32.dp)) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Text(
                text = "Desliza hacia abajo para actualizar.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.LightGray
            )
        }
    }
}
