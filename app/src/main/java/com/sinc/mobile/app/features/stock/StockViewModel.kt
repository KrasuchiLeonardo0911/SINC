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

sealed class DesgloseUiData {
    data class Grouped(val chartData: List<PieChartData>, val legendItems: List<LegendItem>) : DesgloseUiData()
    data class Full(val tableData: List<DesgloseItem.Full>) : DesgloseUiData()
}

data class ProcessedStock(
    val stockTotalGeneral: Int,
    val unidadesProductivas: List<ProcessedUnidadProductivaStock>,
    val allSpecies: List<ProcessedEspecieStock>,
    val speciesDistribution: List<PieChartData> = emptyList(),
    val speciesLegendItems: List<LegendItem> = emptyList() // New field
)

data class ProcessedUnidadProductivaStock(
    val nombre: String,
    val stockTotal: Int
)

data class ProcessedEspecieStock(
    val nombre: String,
    val stockTotal: Int,
    val desglose: List<DesgloseItem.Full>,
    val color: Color // NEW FIELD
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
        Color(0xFF2E7D32), // Verde Fuerte
        Color(0xFF66BB6A), // Verde Claro
        Color(0xFF9CCC65), // Verde Lima Suave
        Color(0xFF26A69A), // Verde Azulado
        Color(0xFFFFA726), // Naranja (Contraste)
        Color(0xFF29B6F6), // Azul Claro (Contraste)
        Color(0xFF78909C), // Gris Azulado
        Color(0xFF8D6E63)  // Marrón Suave
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
            if (duration < 500) { // Ensure spinner is visible for at least 500ms
                delay(500 - duration)
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
                    .map { (key, group) ->
                        DesgloseItem.Full(key.first, key.second, group.sumOf { it.cantidad })
                    }

                // Get the color for this species based on global consistent colors
                // We find the index of this species in the global list of species names to ensure consistent coloring
                // regardless of filtering.
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


