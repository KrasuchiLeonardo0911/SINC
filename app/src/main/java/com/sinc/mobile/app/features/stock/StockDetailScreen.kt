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
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.ui.theme.SincPrimary
import com.sinc.mobile.ui.theme.SincBackground
import com.sinc.mobile.ui.theme.SincGrayBackground
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StockDetailScreen(
    speciesName: String,
    grouping: String,
    onBack: () -> Unit,
    viewModel: StockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Estado local para el agrupamiento (inicializado con el parámetro de navegación)
    var currentGrouping by remember { 
        mutableStateOf(
            try { StockGrouping.valueOf(grouping) } catch (e: Exception) { StockGrouping.BY_ALL }
        )
    }

    val detailData = remember(uiState.processedStock, speciesName, currentGrouping) {
        viewModel.getSpeciesDetailData(speciesName, currentGrouping)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        containerColor = Color.White,
        topBar = {
            MinimalHeader(
                title = "Stock de $speciesName",
                onBackPress = onBack,
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { paddingValues ->
        if (detailData == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SincPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Espacio entre el header y el selector (Franja Gris)
                Spacer(modifier = Modifier.height(24.dp))

                // Encabezado Formal Estilo Excel / Reporte (Franja Gris)
                StockDetailTabHeader(
                    selectedGrouping = currentGrouping,
                    onGroupingSelected = { currentGrouping = it }
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Lista Estilo Limpio (Basado en CampoListItem)
                    val listData = when(currentGrouping) {
                        StockGrouping.BY_ALL -> detailData.tableData.map { Triple(it.categoria, it.raza, it.quantity) }
                        StockGrouping.BY_CATEGORY -> detailData.legendItems.map { Triple(it.label, "Stock por categoría", it.value) }
                        StockGrouping.BY_BREED -> detailData.legendItems.map { Triple(it.label, "Stock por raza", it.value) }
                    }

                    itemsIndexed(listData) { index, (title, subtitle, value) ->
                        StockListItem(
                            title = title,
                            subtitle = subtitle,
                            value = value,
                            isLast = index == listData.size - 1
                        )
                    }

                    // --- Sección de Análisis Visual (Gráficos) ---
                    if (currentGrouping != StockGrouping.BY_ALL && detailData.chartData.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (currentGrouping == StockGrouping.BY_CATEGORY) "DISTRIBUCIÓN POR CATEGORÍA" else "DISTRIBUCIÓN POR RAZA",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color.Gray,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                                    textAlign = TextAlign.Center
                                )

                                // Gráfico de Dona
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .shadow(elevation = 4.dp, shape = CircleShape)
                                        .background(Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    PieChart(
                                        data = detailData.chartData,
                                        modifier = Modifier.fillMaxSize(),
                                        strokeWidth = 35f
                                    )
                                    
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = detailData.stockTotal.toString(),
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = SincPrimary
                                        )
                                        Text(
                                            text = "Total",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                // Leyenda del Gráfico
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                    detailData.legendItems.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(item.color)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = item.label,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color(0xFF191C1E)
                                                )
                                            }
                                            Text(
                                                text = "${item.percentage.roundToInt()}% (${item.value})",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockDetailTabHeader(
    selectedGrouping: StockGrouping,
    onGroupingSelected: (StockGrouping) -> Unit
) {
    val options = listOf(
        StockGrouping.BY_ALL to "TOTAL",
        StockGrouping.BY_CATEGORY to "CATEGORÍA",
        StockGrouping.BY_BREED to "RAZA"
    )
    
    val selectedIndex = options.indexOfFirst { it.first == selectedGrouping }

    Column(modifier = Modifier.fillMaxWidth().background(SincGrayBackground)) {
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = SincGrayBackground,
            contentColor = SincPrimary,
            indicator = { tabPositions ->
                if (selectedIndex != -1) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = SincPrimary,
                        height = 2.dp
                    )
                }
            },
            divider = {} // Eliminamos el divisor por defecto para usar el nuestro más fino
        ) {
            options.forEachIndexed { index, (grouping, label) ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { onGroupingSelected(grouping) },
                    text = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            ),
                            color = if (selectedIndex == index) SincPrimary else Color.Gray,
                            maxLines = 1
                        )
                    }
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun StockListItem(
    title: String,
    subtitle: String,
    value: Int,
    isLast: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SincPrimary
                )
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
