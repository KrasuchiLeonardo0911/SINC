package com.sinc.mobile.app.features.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.sinc.mobile.app.features.stock.components.PieChart
import com.sinc.mobile.app.features.stock.components.PieChartData
import com.sinc.mobile.app.features.stock.components.LegendItem
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.ui.theme.SincPrimary
import com.sinc.mobile.ui.theme.SincGrayBackground
import kotlin.math.roundToInt
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import com.sinc.mobile.app.navigation.Routes


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    speciesName: String,
    grouping: String,
    unidadId: Int? = null,
    onBack: () -> Unit,
    navController: NavHostController,
    viewModel: StockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Sincronizar la selección de unidad si viene por parámetro
    LaunchedEffect(unidadId) {
        if (unidadId != uiState.selectedUnidadId) {
            viewModel.selectUnidad(unidadId)
        }
    }

    val detailData = remember(uiState.processedStock, speciesName) {
        viewModel.getSpeciesDetailData(speciesName)
    }

    // Estado para el modal de venta
    var showSaleSheet by remember { mutableStateOf(false) }
    var selectedItemForSale by remember { mutableStateOf<DesgloseItem.Full?>(null) }
    var pesoVenta by remember { mutableStateOf("") }
    var observacionesVenta by remember { mutableStateOf("") }

    LaunchedEffect(uiState.saleSuccess, uiState.saleError) {
        if (uiState.saleSuccess != null || uiState.saleError != null) {
            showSaleSheet = false
            uiState.saleSuccess?.let { snackbarHostState.showSnackbar(it) }
            uiState.saleError?.let { snackbarHostState.showSnackbar(it) }
            viewModel.clearSaleMessages()
        }
    }

    val selectedUnidad = uiState.unidadesProductivas.find { it.id == uiState.selectedUnidadId }

    Scaffold(
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MinimalHeader(
                title = if (selectedUnidad != null) "Detalles: ${selectedUnidad.nombre}" else "Detalles Generales",
                onBackPress = onBack,
                modifier = Modifier.statusBarsPadding(),
                titleFontSize = 16.sp
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (detailData == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SincPrimary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // 1. Título de Sección y Encabezado de Tabla
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 24.dp)
                        ) {
                            Text(
                                text = speciesName.uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Text(
                                text = "Existencias detalladas en el sistema",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }

                        // Encabezado de Tabla Formal
                        TableHeader()
                    }

                    // 2. Lista de Animales (Tabla)
                    if (detailData.tableData.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text("No hay datos disponibles.", color = Color.LightGray)
                            }
                        }
                    } else {
                        itemsIndexed(detailData.tableData) { index, originalItem ->
                            StockListItem(
                                title = originalItem.categoria,
                                subtitle = originalItem.raza,
                                value = originalItem.quantity,
                                isLast = index == detailData.tableData.size - 1,
                                onSellClick = {
                                    if (uiState.selectedUnidadId != null) {
                                        if (uiState.isLogisticsOpen) {
                                            selectedItemForSale = originalItem
                                            pesoVenta = ""
                                            observacionesVenta = ""
                                            showSaleSheet = true
                                        } else {
                                            viewModel.viewModelScope.launch {
                                                snackbarHostState.showSnackbar("El periodo de ventas ha cerrado, espere hasta el siguiente ciclo.")
                                            }
                                        }
                                    } else {
                                        viewModel.viewModelScope.launch {
                                            snackbarHostState.showSnackbar("Para vender, debe seleccionar un campo en la pantalla anterior.")
                                        }
                                    }
                                },
                                onAdjustClick = {
                                    navController.navigate(
                                        Routes.createMovimientoFormRoute(
                                            unidadId = uiState.selectedUnidadId?.toString(),
                                            initialPage = 0,
                                            especieId = originalItem.especieId,
                                            razaId = originalItem.razaId,
                                            categoriaId = originalItem.categoriaId
                                        )
                                    )
                                },
                                isLogisticsOpen = uiState.isLogisticsOpen
                            )
                        }
                    }

                    // 3. SECCIÓN: RESUMEN POR CATEGORÍA
                    item {
                        SectionDivider("RESUMEN POR CATEGORÍA")
                        SummarySection(
                            total = detailData.stockTotal,
                            chartData = detailData.categoryChart,
                            legendItems = detailData.categoryLegend
                        )
                    }

                    // 4. SECCIÓN: RESUMEN POR RAZA
                    item {
                        SectionDivider("RESUMEN POR RAZA")
                        SummarySection(
                            total = detailData.stockTotal,
                            chartData = detailData.breedChart,
                            legendItems = detailData.breedLegend
                        )
                    }
                }
            }
        }
    }

    // Modal de Venta
    if (showSaleSheet && selectedItemForSale != null) {
        ModalBottomSheet(
            onDismissRequest = { showSaleSheet = false },
            sheetState = rememberModalBottomSheetState(),
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
                    color = SincPrimary
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                                especieNombre = speciesName,
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
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SincGrayBackground)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "ANIMAL / DETALLE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "ACCIONES",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = "STOCK",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            textAlign = TextAlign.End,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
private fun SectionDivider(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(SincGrayBackground)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun SummarySection(
    total: Int,
    chartData: List<PieChartData>,
    legendItems: List<LegendItem>
) {
    if (chartData.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            PieChart(
                data = chartData,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 40f
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = total.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Leyenda del gráfico
        legendItems.forEachIndexed { index, item: LegendItem ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(item.color)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${item.percentage.roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (index < legendItems.size - 1) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun StockListItem(
    title: String,
    subtitle: String,
    value: Int,
    isLast: Boolean,
    onSellClick: (() -> Unit)? = null,
    onAdjustClick: (() -> Unit)? = null,
    isLogisticsOpen: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
            
            // Iconos de acción (Después del nombre)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.width(80.dp)
            ) {
                if (onAdjustClick != null) {
                    IconButton(
                        onClick = onAdjustClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Ajustar",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (onSellClick != null) {
                    IconButton(
                        onClick = onSellClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Vender",
                            tint = if (isLogisticsOpen) Color(0xFF2E7D32) else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Valor total (Al final a la derecha)
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SincPrimary
                ),
                textAlign = TextAlign.End,
                modifier = Modifier.width(60.dp)
            )
        }
    }
    
    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}
