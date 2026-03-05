package com.sinc.mobile.app.features.stock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sinc.mobile.app.features.stock.components.GroupingOptions
import com.sinc.mobile.app.features.stock.components.LegendItem
import com.sinc.mobile.app.features.stock.components.PieChart
import com.sinc.mobile.app.features.stock.components.PieChartData
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.app.ui.components.FullscreenLoader
import com.sinc.mobile.ui.theme.SincPrimary
import com.sinc.mobile.ui.theme.SincBackground
import com.sinc.mobile.ui.theme.SincGrayBackground
import com.sinc.mobile.domain.model.UnidadProductiva
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigateToVentas: () -> Unit,
    onNavigateToDetail: (String, String) -> Unit,
    viewModel: StockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val processedStock = uiState.processedStock
    
    var selectedSpeciesForDetail by remember { mutableStateOf<String?>(null) }
    var showGroupingSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

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
                if (processedStock != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .pullRefresh(pullRefreshState),
                        contentPadding = PaddingValues(
                            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 48.dp + 24.dp, 
                            bottom = 24.dp
                        ), 
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        // Sección de Filtros
                        item {
                            StockFilterSection(
                                unidades = uiState.unidadesProductivas,
                                selectedUnidadId = uiState.selectedUnidadId,
                                onSelectUnidad = viewModel::selectUnidad
                            )
                        }

                        // Sección de Stock Total (Interactiva)
                        item {
                            TotalStockSection(
                                stock = processedStock,
                                onSpeciesSelected = { species ->
                                    selectedSpeciesForDetail = species
                                    showGroupingSheet = true
                                }
                            )
                        }
                    }
                } else if (!uiState.isLoading) {
                    EmptyStockState()
                }

                // Header Fijo Superior
                MinimalHeader(
                    title = "Mi Stock",
                    onBackPress = onBack,
                    modifier = Modifier.align(Alignment.TopCenter),
                    actions = {
                        TextButton(
                            onClick = onNavigateToVentas,
                            colors = ButtonDefaults.textButtonColors(contentColor = SincPrimary)
                        ) {
                            Text(text = "Ventas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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

    if (showGroupingSheet && selectedSpeciesForDetail != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                showGroupingSheet = false
                selectedSpeciesForDetail = null
            },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ver stock de $selectedSpeciesForDetail",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191C1E)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Seleccione cómo desea agrupar los datos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val options = listOf(
                        "Vista Total" to "BY_ALL",
                        "Por Categoría" to "BY_CATEGORY",
                        "Por Raza" to "BY_BREED"
                    )
                    
                    options.forEach { (label, value) ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                showGroupingSheet = false
                                onNavigateToDetail(selectedSpeciesForDetail!!, value)
                                selectedSpeciesForDetail = null
                            },
                            label = { 
                                Text(
                                    text = label, 
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    fontSize = 14.sp
                                ) 
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = SincPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = SincPrimary,
                                borderWidth = 1.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StockFilterSection(
    unidades: List<UnidadProductiva>,
    selectedUnidadId: Int?,
    onSelectUnidad: (Int?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Filtrar por Campo:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF424242),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Chip "Todos"
            val isTodosSelected = selectedUnidadId == null
            FilterChip(
                selected = isTodosSelected,
                onClick = { onSelectUnidad(null) },
                label = { 
                    Text(
                        text = "Todos",
                        fontSize = 12.sp,
                        fontWeight = if (isTodosSelected) FontWeight.Bold else FontWeight.Normal
                    ) 
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    labelColor = SincPrimary,
                    selectedContainerColor = SincPrimary,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isTodosSelected,
                    borderColor = SincPrimary,
                    selectedBorderColor = SincPrimary,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp
                )
            )

            // Chips para cada unidad
            unidades.forEach { unidad ->
                val isSelected = selectedUnidadId == unidad.id
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectUnidad(unidad.id) },
                    label = { 
                        Text(
                            text = unidad.nombre ?: "Campo ${unidad.id}",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        labelColor = SincPrimary,
                        selectedContainerColor = SincPrimary,
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = SincPrimary,
                        selectedBorderColor = SincPrimary,
                        borderWidth = 1.dp,
                        selectedBorderWidth = 1.dp
                    )
                )
            }
        }
    }
}

@Composable
private fun TotalStockSection(
    stock: ProcessedStock,
    onSpeciesSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Resumen de Existencias",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF191C1E),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        // Gráfico de Dona con Sombra y Total en el centro
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

        Spacer(modifier = Modifier.height(32.dp))

        // Leyenda en Filas (Estilo Mis Campos)
        if (stock.speciesLegendItems.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                stock.speciesLegendItems.forEachIndexed { index, item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSpeciesSelected(item.label) }
                            .padding(vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(item.color)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF191C1E)
                                    )
                                    Text(
                                        text = "Representa el ${item.percentage.roundToInt()}% del stock",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Ver detalle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    if (index < stock.speciesLegendItems.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStockState() {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No se pudo cargar el stock.",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Text(
                text = "Desliza hacia abajo para reintentar.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color.LightGray
            )
        }
    }
}
