package com.sinc.mobile.app.features.stock

import android.util.Log // Import Log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // Add this import
import androidx.navigation.NavController
import com.sinc.mobile.app.features.stock.components.StockAccordion
import com.sinc.mobile.app.features.stock.components.StockViewSelector
import com.sinc.mobile.app.ui.components.MinimalHeader
import com.sinc.mobile.domain.model.DesgloseStock
import com.sinc.mobile.domain.model.EspecieStock
import com.sinc.mobile.domain.model.Stock
import com.sinc.mobile.domain.model.UnidadProductiva
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(
    stock: Stock?,
    unidadesProductivas: List<UnidadProductiva>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Log.d("StockScreen", "StockScreen recompose. Stock: $stock, Unidades: ${unidadesProductivas.size}, Loading: $isLoading")
    var selectedView by remember { mutableStateOf<Any>("Total") }
    Log.d("StockScreen", "Current selectedView: $selectedView")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MinimalHeader(
                title = "Mi Stock",
                onBackPress = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            StockViewSelector(
                unidades = unidadesProductivas,
                selectedView = selectedView,
                onSelectionChanged = {
                    selectedView = it
                    Log.d("StockScreen", "Selected view changed to: $selectedView")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = onRefresh,
            ) {
                if (stock != null) {
                    Log.d("StockScreen", "Stock data is not null. Total General: ${stock.stockTotalGeneral}")
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        when (val selection = selectedView) {
                            is String -> { // "Total" view
                                if (stock.stockTotalGeneral == 0) {
                                    item { Text("No hay stock registrado en el sistema.", modifier = Modifier.padding(top = 16.dp)) }
                                } else {
                                    val allSpecies = stock.unidadesProductivas
                                        .flatMap { it.especies }
                                        .groupBy { it.nombre }
                                        .map { (nombre, especies) ->
                                            EspecieStock(
                                                nombre = nombre,
                                                stockTotal = especies.sumOf { it.stockTotal },
                                                desglose = especies.flatMap { it.desglose }
                                                    .groupBy { Triple(it.categoria, it.raza, it.cantidad) }
                                                    .map { (key, group) ->
                                                        DesgloseStock(key.first, key.second, group.sumOf { it.cantidad })
                                                    }
                                            )
                                        }
                                    Log.d("StockScreen", "Total view - allSpecies: $allSpecies")

                                    item {
                                        StockAccordion(
                                            header = {
                                                Text("Stock Total General: ${stock.stockTotalGeneral}", fontWeight = FontWeight.Bold)
                                            },
                                            content = {
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    stock.unidadesProductivas.forEach { up ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(up.nombre, fontWeight = FontWeight.SemiBold)
                                                            Text(up.stockTotal.toString())
                                                        }
                                                    }
                                                }
                                            },
                                            initiallyExpanded = true
                                        )
                                    }
                                    items(allSpecies) { especie ->
                                        StockAccordion(
                                            header = { Text("${especie.nombre}: ${especie.stockTotal}", fontWeight = FontWeight.SemiBold) },
                                            content = { DesgloseContent(desglose = especie.desglose) }
                                        )
                                    }
                                }
                            }
                            is UnidadProductiva -> {
                                Log.d("StockScreen", "Selected UnidadProductiva: ${selection.nombre} (ID: ${selection.id})")
                                val upStock = stock.unidadesProductivas.find { it.id == selection.id }
                                if (upStock != null && upStock.stockTotal > 0) {
                                    Log.d("StockScreen", "Found upStock: ${upStock.nombre}, total: ${upStock.stockTotal}")
                                    item {
                                        StockAccordion(
                                            header = {
                                                Text("Stock Total: ${upStock.stockTotal}", fontWeight = FontWeight.Bold)
                                            },
                                            content = {},
                                            initiallyExpanded = true
                                        )
                                    }
                                    items(upStock.especies) { especie ->
                                        Log.d("StockScreen", "Especie in upStock: ${especie.nombre}, total: ${especie.stockTotal}")
                                        StockAccordion(
                                            header = { Text("${especie.nombre}: ${especie.stockTotal}", fontWeight = FontWeight.SemiBold) },
                                            content = { DesgloseContent(desglose = especie.desglose) }
                                        )
                                    }
                                } else {
                                    Log.d("StockScreen", "upStock not found or total is 0 for ID: ${selection.id}. upStock: $upStock")
                                    item { Text("No hay stock registrado en este campo.", modifier = Modifier.padding(top = 16.dp)) }
                                }
                            }
                        }
                    }
                } else if (!isLoading) {
                    Log.d("StockScreen", "Stock data is null and not loading.")
                    // Use a full-size box to allow swipe-to-refresh on an empty screen
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "No se pudo cargar el stock. Desliza hacia abajo para reintentar.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DesgloseContent(desglose: List<DesgloseStock>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tipo", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text("Raza", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text("Cantidad", modifier = Modifier.width(72.dp), fontWeight = FontWeight.Medium, textAlign = TextAlign.End, fontSize = 13.sp)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        // Rows
        desglose.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.categoria, modifier = Modifier.weight(1f))
                Text(item.raza, modifier = Modifier.weight(1f))
                Text(item.cantidad.toString(), modifier = Modifier.width(72.dp), textAlign = TextAlign.End)
            }
        }
    }
}