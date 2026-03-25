package com.sinc.mobile.app.features.stock

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.app.features.stock.components.LegendItem
import com.sinc.mobile.app.features.stock.components.PieChartData
import com.sinc.mobile.app.ui.components.BannerManager
import com.sinc.mobile.app.ui.components.BannerType
import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.model.Stock
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.use_case.GetEffectiveStockUseCase
import com.sinc.mobile.domain.use_case.GetUnidadesProductivasUseCase
import com.sinc.mobile.domain.use_case.SyncStockUseCase
import com.sinc.mobile.domain.use_case.SyncUnidadesProductivasUseCase
import com.sinc.mobile.domain.repository.MovimientoHistorialRepository
import com.sinc.mobile.domain.util.NetworkMonitor
import com.sinc.mobile.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.sinc.mobile.domain.use_case.ventas.CreateDeclaracionVentaUseCase
import com.sinc.mobile.domain.use_case.ventas.SyncDeclaracionesVentaUseCase
import com.sinc.mobile.domain.use_case.ventas.ValidateStockForVentaUseCase
import com.sinc.mobile.domain.model.GenericError
import kotlinx.coroutines.flow.first

// region Data Models
enum class StockGrouping {
    BY_ALL,
    BY_CATEGORY,
    BY_BREED
}

sealed class DesgloseItem { // Renamed for clarity
    data class Full(
        val especieId: Int,
        val categoriaId: Int,
        val razaId: Int,
        val categoria: String, 
        val raza: String, 
        val quantity: Int
    ) : DesgloseItem()
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
 * Representa los datos necesarios para visualizar el detalle completo de una especie en una sola pantalla.
 */
data class SpeciesDetailUiData(
    val speciesName: String,
    val stockTotal: Int,
    val color: Color,
    val tableData: List<DesgloseItem.Full> = emptyList(),
    val categoryChart: List<PieChartData> = emptyList(),
    val categoryLegend: List<LegendItem> = emptyList(),
    val breedChart: List<PieChartData> = emptyList(),
    val breedLegend: List<LegendItem> = emptyList()
)

data class StockUiState(
    val isInitialLoad: Boolean = true,
    val isLoading: Boolean = false,
    val isSelling: Boolean = false,
    val saleSuccess: String? = null,
    val saleError: String? = null,
    val unidadesProductivas: List<UnidadProductiva> = emptyList(),
    val error: String? = null,
    val stock: Stock? = null,
    val processedStock: ProcessedStock? = null,
    val selectedUnidadId: Int? = null,
    val upSearchQuery: String = "",
    val isLogisticsOpen: Boolean = true,
    val hasUnsyncedData: Boolean = false,
    val isOnline: Boolean = true
)
// endregion

@HiltViewModel
class StockViewModel @Inject constructor(
    private val getUnidadesProductivasUseCase: GetUnidadesProductivasUseCase,
    private val syncUnidadesProductivasUseCase: SyncUnidadesProductivasUseCase,
    private val getEffectiveStockUseCase: GetEffectiveStockUseCase,
    private val syncStockUseCase: SyncStockUseCase,
    private val createDeclaracionVentaUseCase: CreateDeclaracionVentaUseCase,
    private val validateStockForVentaUseCase: ValidateStockForVentaUseCase,
    private val syncDeclaracionesVentaUseCase: SyncDeclaracionesVentaUseCase,
    private val ventasRepository: com.sinc.mobile.domain.repository.VentasRepository,
    private val catalogosRepository: com.sinc.mobile.domain.repository.CatalogosRepository,
    private val getLogisticsStatusUseCase: com.sinc.mobile.domain.use_case.ventas.GetLogisticsStatusUseCase,
    private val initializeAppUseCase: com.sinc.mobile.domain.use_case.init.InitializeAppUseCase,
    private val movimientoHistorialRepository: MovimientoHistorialRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockUiState())
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    private var catalogos: Catalogos? = null

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
                getEffectiveStockUseCase(),
                catalogosRepository.getMovimientoCatalogos(),
                movimientoHistorialRepository.getMovimientos(),
                networkMonitor.isOnline
            ) { unidades, stock, catalogosData, historial, isOnline ->
                // Guardamos los catálogos localmente
                catalogos = catalogosData
                
                val currentSelectedId = _uiState.value.selectedUnidadId
                
                val targetSelectedId = if (currentSelectedId == null && unidades.size == 1) {
                    unidades.first().id
                } else {
                    currentSelectedId
                }

                val hasUnsynced = historial.any { !it.sincronizado }
                val processed = stock?.let { processStock(it, targetSelectedId, catalogosData) }
                
                _uiState.update {
                    it.copy(
                        unidadesProductivas = unidades,
                        stock = stock,
                        processedStock = processed,
                        selectedUnidadId = targetSelectedId,
                        hasUnsyncedData = hasUnsynced,
                        isOnline = isOnline
                    )
                }
            }.launchIn(this)
        }

        // Logic to show banner based on state changes
        viewModelScope.launch {
            uiState.map { Pair(it.isOnline, it.hasUnsyncedData) }
                .distinctUntilChanged()
                .collect { (isOnline, hasUnsynced) ->
                    // Show banner only after initial load is complete
                    if (!_uiState.value.isInitialLoad) {
                        if (!isOnline) {
                            BannerManager.show(
                                "Modo offline: No hay conexión a internet.",
                                BannerType.WARNING
                            )
                        } else if (hasUnsynced) {
                            BannerManager.show(
                                "Tienes movimientos locales sin sincronizar.",
                                BannerType.WARNING
                            )
                        }
                    }
                }
        }

        initialSync()
    }

    private fun checkLogisticsStatus() {
        val isOpen = getLogisticsStatusUseCase()
        _uiState.update { it.copy(isLogisticsOpen = isOpen) }
    }

    private fun initialSync() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            try {
                initializeAppUseCase()
                syncStockUseCase()
                syncUnidadesProductivasUseCase()
                syncDeclaracionesVentaUseCase()
                checkLogisticsStatus()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error de conexión") }
            } finally {
                val duration = System.currentTimeMillis() - startTime
                if (duration < 1000) { 
                    delay(1000 - duration)
                }
                _uiState.update { it.copy(isInitialLoad = false) }
            }
        }
    }

    fun onUpSearchQueryChange(query: String) {
        _uiState.update { it.copy(upSearchQuery = query) }
    }

    fun refresh() {
        viewModelScope.launch {
            if (_uiState.value.isLoading) return@launch
            _uiState.update { it.copy(isLoading = true, error = null) }
            val startTime = System.currentTimeMillis()

            initializeAppUseCase()
            val stockSyncResult = syncStockUseCase()
            syncUnidadesProductivasUseCase()
            syncDeclaracionesVentaUseCase()
            checkLogisticsStatus()

            if (stockSyncResult is Result.Failure) {
                _uiState.update { it.copy(error = stockSyncResult.error.message) }
            }

            val duration = System.currentTimeMillis() - startTime
            if (duration < 1000) {
                delay(1000 - duration)
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onSellAnimal(
        especieNombre: String,
        categoriaNombre: String,
        razaNombre: String,
        cantidad: Int,
        peso: Float?,
        observaciones: String,
        unidadId: Int
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSelling = true, saleError = null, saleSuccess = null) }

            if (!getLogisticsStatusUseCase()) {
                _uiState.update { 
                    it.copy(
                        isSelling = false, 
                        isLogisticsOpen = false,
                        saleError = "El periodo de inscripciones ha cerrado. No se pueden realizar ventas en este momento."
                    ) 
                }
                return@launch
            }

            val declaracionesActuales = ventasRepository.getDeclaraciones().first()
            val advancedLogistics = declaracionesActuales.any { 
                it.estado == "recogido" || it.estado == "en-matadero" || it.estado == "entregado" 
            }

            if (advancedLogistics) {
                _uiState.update { 
                    it.copy(
                        isSelling = false, 
                        saleError = "El camión ya está en camino o el lote está en planta. No se pueden añadir más animales en este ciclo."
                    ) 
                }
                return@launch
            }

            val catalogos = catalogosRepository.getMovimientoCatalogos().first()
            val especieId = catalogos.especies.find { it.nombre.equals(especieNombre, ignoreCase = true) }?.id
            val categoriaId = catalogos.categorias.find { it.nombre.equals(categoriaNombre, ignoreCase = true) && it.especieId == especieId }?.id
            val razaId = catalogos.razas.find { it.nombre.equals(razaNombre, ignoreCase = true) && it.especieId == especieId }?.id

            if (especieId == null || categoriaId == null || razaId == null) {
                _uiState.update { it.copy(isSelling = false, saleError = "Error al identificar el animal en el catálogo.") }
                return@launch
            }

            val validationResult = validateStockForVentaUseCase(
                unidadProductivaId = unidadId,
                especieId = especieId,
                razaId = razaId,
                categoriaAnimalId = categoriaId,
                cantidadSolicitada = cantidad
            )

            when (validationResult) {
                is ValidateStockForVentaUseCase.ValidationResult.Success -> {
                    val result = createDeclaracionVentaUseCase(
                        unidadProductivaId = unidadId,
                        especieId = especieId,
                        razaId = razaId,
                        categoriaAnimalId = categoriaId,
                        cantidad = cantidad,
                        observaciones = observaciones,
                        pesoAproximadoKg = peso
                    )

                    when (result) {
                        is Result.Success -> {
                            _uiState.update { 
                                it.copy(
                                    isSelling = false, 
                                    saleSuccess = "Venta registrada correctamente."
                                )
                            }
                            syncDeclaracionesVentaUseCase()
                            syncStockUseCase()
                        }
                        is Result.Failure -> {
                            val rawMsg = (result.error as? GenericError)?.message ?: "Error al guardar la venta"
                            val msg = if (rawMsg.contains("periodo", ignoreCase = true) || rawMsg.contains("cerrado", ignoreCase = true)) {
                                "El periodo de ventas ha cerrado, espere hasta el siguiente ciclo."
                            } else {
                                rawMsg
                            }
                            _uiState.update { it.copy(isSelling = false, saleError = msg) }
                        }
                    }
                }
                is ValidateStockForVentaUseCase.ValidationResult.InsufficientStock -> {
                    val msg = "Stock insuficiente. Disponible: ${validationResult.real - validationResult.pendiente}"
                    _uiState.update { it.copy(isSelling = false, saleError = msg) }
                }
                is ValidateStockForVentaUseCase.ValidationResult.Error -> {
                    _uiState.update { it.copy(isSelling = false, saleError = validationResult.message) }
                }
            }
        }
    }

    fun clearSaleMessages() {
        _uiState.update { it.copy(saleError = null, saleSuccess = null) }
    }

    fun selectUnidad(unidadId: Int?) {
        _uiState.update { currentState ->
            val processed = currentState.stock?.let { processStock(it, unidadId, catalogos) }
            currentState.copy(
                selectedUnidadId = unidadId,
                processedStock = processed
            )
        }
    }

    fun getSpeciesDetailData(speciesName: String): SpeciesDetailUiData? {
        val processedStock = _uiState.value.processedStock ?: return null
        val speciesStock = processedStock.allSpecies.find { it.nombre == speciesName } ?: return null

        val totalStock = speciesStock.stockTotal.toFloat()
        
        val byCategory = speciesStock.desglose.groupBy { it.categoria }
            .mapValues { it.value.sumOf { item -> item.quantity } }
            .toSortedMap()
        val catDistribution = calculateDistribution(byCategory, totalStock)

        val byBreed = speciesStock.desglose.groupBy { it.raza }
            .mapValues { it.value.sumOf { item -> item.quantity } }
            .toSortedMap()
        val breedDistribution = calculateDistribution(byBreed, totalStock)

        return SpeciesDetailUiData(
            speciesName = speciesName,
            stockTotal = speciesStock.stockTotal,
            color = speciesStock.color,
            tableData = speciesStock.desglose,
            categoryChart = catDistribution.first,
            categoryLegend = catDistribution.second,
            breedChart = breedDistribution.first,
            breedLegend = breedDistribution.second
        )
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

    internal fun processStock(stock: Stock, selectedUnidadId: Int?, catalogos: Catalogos?): ProcessedStock {
        val filteredUnits = if (selectedUnidadId == null) {
            stock.unidadesProductivas
        } else {
            stock.unidadesProductivas.filter { it.id == selectedUnidadId }
        }

        val speciesTotals = filteredUnits
            .flatMap { it.especies }
            .groupBy { it.nombre }
            .mapValues { entry -> entry.value.sumOf { it.stockTotal } }
            .toSortedMap()
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
                val especieId = catalogos?.especies?.find { esp -> esp.nombre.equals(nombreEspecie, ignoreCase = true) }?.id ?: -1

                val desgloses = especiesList.flatMap { it.desglose }
                    .groupBy { Pair(it.categoria, it.raza) }
                    .mapNotNull { (key, group) ->
                        val sumQuantity = group.sumOf { item -> item.cantidad }
                        if (sumQuantity > 0) {
                            val categoriaId = catalogos?.categorias?.find { cat -> 
                                cat.nombre.equals(key.first, ignoreCase = true) && cat.especieId == especieId 
                            }?.id ?: -1
                            val razaId = catalogos?.razas?.find { rz -> 
                                rz.nombre.equals(key.second, ignoreCase = true) && rz.especieId == especieId 
                            }?.id ?: -1

                            DesgloseItem.Full(
                                especieId = especieId,
                                categoriaId = categoriaId,
                                razaId = razaId,
                                categoria = key.first, 
                                raza = key.second, 
                                quantity = sumQuantity
                            )
                        } else {
                            null
                        }
                    }

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
