package com.sinc.mobile.app.features.stock

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.app.features.stock.components.LegendItem
import com.sinc.mobile.app.features.stock.components.PieChartData
import com.sinc.mobile.domain.model.Stock
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.use_case.GetStockUseCase
import com.sinc.mobile.domain.use_case.GetUnidadesProductivasUseCase
import com.sinc.mobile.domain.use_case.SyncStockUseCase
import com.sinc.mobile.domain.use_case.SyncUnidadesProductivasUseCase
import com.sinc.mobile.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// region Data Models
enum class StockGrouping {
    BY_ALL,
    BY_CATEGORY,
    BY_BREED
}

sealed class DesgloseItem { // Renamed for clarity
    data class Full(val categoria: String, val raza: String, val quantity: Int) : DesgloseItem()
}

data class ProcessedStock(
    val stockTotalGeneral: Int,
    val unidadesProductivas: List<ProcessedUnidadProductivaStock>,
    val allSpecies: List<ProcessedEspecieStock>,
    val speciesDistribution: List<PieChartData> = emptyList(),
    val speciesLegendItems: List<LegendItem> = emptyList()
)

data class ProcessedUnidadProductivaStock(
    val nombre: String,
    val stockTotal: Int
)

data class ProcessedEspecieStock(
    val nombre: String,
    val stockTotal: Int,
    val desglose: List<DesgloseItem.Full>,
    val color: Color
)

/**
 * Representa los datos necesarios para visualizar el detalle de una especie.
 */
data class SpeciesDetailUiData(
    val speciesName: String,
    val stockTotal: Int,
    val color: Color,
    val chartData: List<PieChartData> = emptyList(),
    val legendItems: List<LegendItem> = emptyList(),
    val tableData: List<DesgloseItem.Full> = emptyList()
)

data class StockUiState(
    val isInitialLoad: Boolean = true,
    val isLoading: Boolean = false,
    val unidadesProductivas: List<UnidadProductiva> = emptyList(),
    val error: String? = null,
    val stock: Stock? = null,
    val processedStock: ProcessedStock? = null,
    val selectedUnidadId: Int? = null
)
// endregion

@HiltViewModel
class StockViewModel @Inject constructor(
    private val getUnidadesProductivasUseCase: GetUnidadesProductivasUseCase,
    private val syncUnidadesProductivasUseCase: SyncUnidadesProductivasUseCase,
    private val getStockUseCase: GetStockUseCase,
    private val syncStockUseCase: SyncStockUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    private val pieChartColors = listOf(
        Color(0xFF8C2218), // Bordó Principal (Asignado a Caprinos por orden alfabético)
        Color(0xFF2E7D32), // Verde Oscuro (Asignado a Ovinos por orden alfabético)
        Color(0xFF43A047), // Verde Medio
        Color(0xFF66BB6A), // Verde Claro
        Color(0xFF81C784), // Verde Pálido
        Color(0xFF546E7A), // Gris Azulado
        Color(0xFF78909C), // Gris Medio
        Color(0xFF90A4AE)  // Gris Pálido
    )

    init {
        // This flow collection will update the UI with data from the DB whenever it changes.
        viewModelScope.launch {
            combine(
                getUnidadesProductivasUseCase(),
                getStockUseCase()
            ) { unidades, stock ->
                val currentSelectedId = _uiState.value.selectedUnidadId
                val processed = stock?.let { processStock(it, currentSelectedId) }
                _uiState.update {
                    it.copy(
                        unidadesProductivas = unidades,
                        stock = stock,
                        processedStock = processed,
                    )
                }
            }.launchIn(this)
        }

        // Perform the initial sync, managing the initial loading spinner
        initialSync()
    }

    private fun initialSync() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            syncStockUseCase()
            syncUnidadesProductivasUseCase()
            val duration = System.currentTimeMillis() - startTime
            if (duration < 1500) { // Garantizar visibilidad de 1.5s
                delay(1500 - duration)
            }
            _uiState.update { it.copy(isInitialLoad = false) }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            if (_uiState.value.isLoading) return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            val startTime = System.currentTimeMillis()

            // Perform network sync
            val stockSyncResult = syncStockUseCase()
            syncUnidadesProductivasUseCase()

            if (stockSyncResult is Result.Failure) {
                _uiState.update { it.copy(error = stockSyncResult.error.message) }
            }

            val duration = System.currentTimeMillis() - startTime
            if (duration < 1000) {
                delay(1000 - duration)
            }

            // End loading state
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun selectUnidad(unidadId: Int?) {
        _uiState.update { currentState ->
            val processed = currentState.stock?.let { processStock(it, unidadId) }
            currentState.copy(
                selectedUnidadId = unidadId,
                processedStock = processed
            )
        }
    }

    /**
     * Procesa los datos de detalle para una especie y un agrupamiento específico.
     */
    fun getSpeciesDetailData(speciesName: String, grouping: StockGrouping): SpeciesDetailUiData? {
        val processedStock = _uiState.value.processedStock ?: return null
        val speciesStock = processedStock.allSpecies.find { it.nombre == speciesName } ?: return null

        val totalStock = speciesStock.stockTotal.toFloat()
        
        return when (grouping) {
            StockGrouping.BY_ALL -> {
                SpeciesDetailUiData(
                    speciesName = speciesName,
                    stockTotal = speciesStock.stockTotal,
                    color = speciesStock.color,
                    tableData = speciesStock.desglose
                )
            }
            StockGrouping.BY_CATEGORY -> {
                val byCategory = speciesStock.desglose.groupBy { it.categoria }
                    .mapValues { it.value.sumOf { item -> item.quantity } }
                    .toSortedMap()
                
                val distribution = calculateDistribution(byCategory, totalStock)
                
                SpeciesDetailUiData(
                    speciesName = speciesName,
                    stockTotal = speciesStock.stockTotal,
                    color = speciesStock.color,
                    chartData = distribution.first,
                    legendItems = distribution.second,
                    tableData = speciesStock.desglose
                )
            }
            StockGrouping.BY_BREED -> {
                val byBreed = speciesStock.desglose.groupBy { it.raza }
                    .mapValues { it.value.sumOf { item -> item.quantity } }
                    .toSortedMap()
                
                val distribution = calculateDistribution(byBreed, totalStock)

                SpeciesDetailUiData(
                    speciesName = speciesName,
                    stockTotal = speciesStock.stockTotal,
                    color = speciesStock.color,
                    chartData = distribution.first,
                    legendItems = distribution.second,
                    tableData = speciesStock.desglose
                )
            }
        }
    }

    private fun calculateDistribution(
        data: Map<String, Int>, 
        total: Float
    ): Pair<List<PieChartData>, List<LegendItem>> {
        if (total == 0f) return emptyList<PieChartData>() to emptyList<LegendItem>()

        val chartData = data.entries.mapIndexed { index, entry ->
            PieChartData(
                value = entry.value.toFloat(),
                color = pieChartColors[index % pieChartColors.size]
            )
        }

        val legendItems = data.entries.mapIndexed { index, entry ->
            LegendItem(
                label = entry.key,
                value = entry.value,
                percentage = (entry.value / total) * 100,
                color = pieChartColors[index % pieChartColors.size]
            )
        }

        return chartData to legendItems
    }

    internal fun processStock(stock: Stock, selectedUnidadId: Int?): ProcessedStock {
        // Filter units based on selection
        val filteredUnits = if (selectedUnidadId == null) {
            stock.unidadesProductivas
        } else {
            stock.unidadesProductivas.filter { it.id == selectedUnidadId }
        }

        val speciesTotals = filteredUnits
            .flatMap { it.especies }
            .groupBy { it.nombre }
            .mapValues { entry -> entry.value.sumOf { it.stockTotal } }
            .toSortedMap() // Ordenar alfabéticamente por especie
        val totalGeneralStock = speciesTotals.values.sum().toFloat()

        val speciesDistributionData = speciesTotals.entries.mapIndexed { index, entry ->
            PieChartData(
                value = entry.value.toFloat(),
                color = pieChartColors[index % pieChartColors.size]
            )
        }

        val speciesLegendItems = if (totalGeneralStock == 0f) {
            emptyList()
        } else {
            speciesTotals.entries.mapIndexed { index, entry ->
                LegendItem(
                    label = entry.key,
                    value = entry.value,
                    percentage = (entry.value / totalGeneralStock) * 100,
                    color = pieChartColors[index % pieChartColors.size]
                )
            }
        }

        val allSpeciesProcessed = filteredUnits
            .flatMap { it.especies }
            .groupBy { it.nombre }
            .map { (nombreEspecie, especiesList) ->
                val desgloses = especiesList.flatMap { it.desglose }
                    .groupBy { Pair(it.categoria, it.raza) }
                    .mapNotNull { (key, group) ->
                        val sumQuantity = group.sumOf { it.cantidad }
                        if (sumQuantity > 0) {
                            DesgloseItem.Full(key.first, key.second, sumQuantity)
                        } else {
                            null
                        }
                    }

                // Get the color for this species based on global consistent colors
                val globalSpeciesList = stock.unidadesProductivas.flatMap { it.especies }.map { it.nombre }.distinct().sorted()
                val colorIndex = globalSpeciesList.indexOf(nombreEspecie).takeIf { it >= 0 } ?: 0
                val speciesColor = pieChartColors[colorIndex % pieChartColors.size]

                ProcessedEspecieStock(
                    nombre = nombreEspecie,
                    stockTotal = especiesList.sumOf { it.stockTotal },
                    desglose = desgloses,
                    color = speciesColor
                )
            }

        val unidadesProcessed = stock.unidadesProductivas.map {
            ProcessedUnidadProductivaStock(nombre = it.nombre, stockTotal = it.stockTotal)
        }

        return ProcessedStock(
            stockTotalGeneral = totalGeneralStock.toInt(),
            unidadesProductivas = unidadesProcessed,
            allSpecies = allSpeciesProcessed,
            speciesDistribution = speciesDistributionData,
            speciesLegendItems = speciesLegendItems
        )
    }
}


