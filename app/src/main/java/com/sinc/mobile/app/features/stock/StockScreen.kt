package com.sinc.mobile.app.features.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
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
    onNavigateToDetail: (String, String, Int?) -> Unit,
    viewModel: StockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val processedStock = uiState.processedStock
    var showUpSelector by remember { mutableStateOf(false) }

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
                    // Selector de Campo (Estilo Barra de Búsqueda)
                    item {
                        val selectedUnidad = uiState.unidadesProductivas.find { it.id == uiState.selectedUnidadId }
                        CampoSelector(
                            selectedUnidadName = selectedUnidad?.nombre ?: "Todos los campos",
                            onClick = { showUpSelector = true }
                        )
                        Spacer(modifier = Modifier.height(64.dp))
                    }

                    if (processedStock != null && processedStock.stockTotalGeneral > 0) {
                        // Sección de Stock Total (Interactiva)
                        item {
                            TotalStockSection(
                                stock = processedStock,
                                onSpeciesSelected = { species ->
                                    onNavigateToDetail(species, "BY_ALL", uiState.selectedUnidadId)
                                }
                            )
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
private fun TotalStockSection(
    stock: ProcessedStock,
    onSpeciesSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 8.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
