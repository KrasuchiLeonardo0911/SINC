package com.sinc.mobile.app.features.movimiento

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.model.MovimientoPendiente
import com.sinc.mobile.domain.model.Stock
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.repository.StockRepository
import com.sinc.mobile.domain.use_case.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import javax.inject.Inject

// Core data classes for this feature's state
data class MovimientoAgrupado(
    val unidadProductivaId: Int,
    val especieId: Int,
    val categoriaId: Int,
    val razaId: Int,
    val motivoMovimientoId: Int,
    val cantidadTotal: Int,
    val destinoTraslado: String?,
    val originales: List<MovimientoPendiente>
)

data class MovimientoStepperState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val selectedUnidad: UnidadProductiva? = null,
    val formManager: MovimientoFormManager? = null,
    val syncState: MovimientoSyncState = MovimientoSyncState(),
    val catalogos: Catalogos? = null,
    val unidades: List<UnidadProductiva> = emptyList(), // Add this
    val stock: Stock? = null,
    val stockValidationError: String? = null
)

@OptIn(ExperimentalFoundationApi::class)
@HiltViewModel
class MovimientoStepperViewModel @Inject constructor(
    private val getUnidadesProductivasUseCase: GetUnidadesProductivasUseCase,
    private val getMovimientoCatalogosUseCase: GetMovimientoCatalogosUseCase,
    private val saveMovimientoLocalUseCase: SaveMovimientoLocalUseCase,
    getMovimientosPendientesUseCase: GetMovimientosPendientesUseCase,
    private val syncMovimientosPendientesUseCase: SyncMovimientosPendientesUseCase,
    private val deleteMovimientoLocalUseCase: DeleteMovimientoLocalUseCase,
    private val stockRepository: StockRepository, // Keep for getStock()
    private val syncStockUseCase: SyncStockUseCase, // Add this
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovimientoStepperState())
    val uiState = _uiState.asStateFlow()

    private val _navigateToPage = MutableSharedFlow<Int>()
    val navigateToPage = _navigateToPage.asSharedFlow()

    private val syncManager: MovimientoSyncManager

    private var catalogos: Catalogos? = null
    private val unidadId: String? = savedStateHandle.get("unidadId")

    init {
        syncManager = MovimientoSyncManager(
            getMovimientosPendientesUseCase,
            syncMovimientosPendientesUseCase,
            deleteMovimientoLocalUseCase,
            syncStockUseCase,
            viewModelScope
        )

        viewModelScope.launch {
            syncManager.syncState.collect {
                _uiState.value = _uiState.value.copy(syncState = it)
            }
        }

        loadInitialData()
    }

    private fun loadInitialData() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            // Explicitly define types to help Kapt
            val flow: Flow<Triple<List<UnidadProductiva>, Catalogos, Stock>> = combine(
                getUnidadesProductivasUseCase(),
                getMovimientoCatalogosUseCase(),
                stockRepository.getStock()
            ) { unidades, catalogos, stock ->
                Triple(unidades, catalogos, stock)
            }
            val initialData: Triple<List<UnidadProductiva>, Catalogos, Stock> = flow.first()

            // Artificial delay to ensure spinner is visible
            delay(400)

            // Update the state all at once
            val (unidades, catalogosData, stockData) = initialData
            val selectedUnidad = unidades.find { it.id.toString() == unidadId }
            catalogos = catalogosData

            _uiState.value = _uiState.value.copy(
                selectedUnidad = selectedUnidad,
                formManager = MovimientoFormManager(catalogosData),
                catalogos = catalogosData,
                unidades = unidades, // Populate units
                stock = stockData,
                isLoading = false
            )
        }
    }

    fun onAddToList() {
        val formManager = _uiState.value.formManager ?: return
        val formState = formManager.formState.value
        val unidad = _uiState.value.selectedUnidad ?: return

        formManager.onDestinoChanged(formState.destino)
        val currentFormState = formManager.formState.value

        if (!currentFormState.isFormValid) {
            return
        }

        val motivo = currentFormState.selectedMotivo
        val isBaja = motivo?.tipo?.equals("baja", ignoreCase = true) == true

        if (!isBaja) {
            proceedToAddToList()
            return
        }

        // --- START VALIDATION LOGIC for "baja" ---
        val stock = _uiState.value.stock ?: run {
            _uiState.value = _uiState.value.copy(stockValidationError = "No se pudo verificar el stock. Intente de nuevo.")
            return
        }

        val especieId = currentFormState.selectedEspecie!!.id
        val categoriaId = currentFormState.selectedCategoria!!.id
        val razaId = currentFormState.selectedRaza!!.id
        val cantidadADescontar = currentFormState.cantidad.toIntOrNull() ?: 0

        // 1. Find current stock for the item by traversing the nested structure
        val especieName = currentFormState.selectedEspecie!!.nombre
        val categoriaName = currentFormState.selectedCategoria!!.nombre
        val razaName = currentFormState.selectedRaza!!.nombre

        val stockActual = stock.unidadesProductivas
            .find { it.id == unidad.id }
            ?.especies?.find { it.nombre.equals(especieName, ignoreCase = true) }
            ?.desglose?.find {
                it.categoria.equals(categoriaName, ignoreCase = true) && it.raza.equals(razaName, ignoreCase = true)
            }
            ?.cantidad ?: 0

        // 2. Find pending bajas for the same item
        val bajasPendientes = _uiState.value.syncState.movimientosAgrupados
            .filter {
                it.especieId == especieId &&
                        it.categoriaId == categoriaId &&
                        it.razaId == razaId &&
                        it.unidadProductivaId == unidad.id &&
                        _uiState.value.catalogos?.motivosMovimiento?.find { m -> m.id == it.motivoMovimientoId }?.tipo?.equals("baja", ignoreCase = true) == true
            }
            .sumOf { it.cantidadTotal }

        // 3. Apply validation rule
        val stockDisponible = stockActual - bajasPendientes
        if (cantidadADescontar > stockDisponible) {
            val errorMessage = "Stock insuficiente. Disponible: $stockDisponible (Actual: $stockActual, Pendientes: $bajasPendientes)"
            _uiState.value = _uiState.value.copy(stockValidationError = errorMessage)
            return // Stop the process
        }
        // --- END VALIDATION LOGIC ---
        proceedToAddToList()
    }

    private fun proceedToAddToList() {
        val formManager = _uiState.value.formManager ?: return
        val formState = formManager.formState.value
        val unidad = _uiState.value.selectedUnidad ?: return
        
        val especieId = formState.selectedEspecie!!.id
        val categoriaId = formState.selectedCategoria!!.id
        val razaId = formState.selectedRaza!!.id
        val motivoId = formState.selectedMotivo!!.id
        val cantidadNum = formState.cantidad.toIntOrNull() ?: 0

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val movimiento = MovimientoPendiente(
                    id = 0,
                    unidadProductivaId = unidad.id,
                    especieId = especieId,
                    categoriaId = categoriaId,
                    razaId = razaId,
                    cantidad = cantidadNum,
                    motivoMovimientoId = motivoId,
                    destinoTraslado = formState.destino.takeIf { it.isNotBlank() },
                    observaciones = null,
                    fechaRegistro = LocalDateTime.now(),
                    sincronizado = false
                )
                saveMovimientoLocalUseCase(movimiento).onSuccess {
                    _uiState.value = _uiState.value.copy(formManager = MovimientoFormManager(catalogos))
                    _navigateToPage.emit(1)
                }.onFailure {
                    // TODO: Show error in UI
                }
            } catch (e: Exception) {
                // Should not happen with the new logic, but kept for safety
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun onSync() {
        syncManager.syncMovements()
    }

    fun deleteMovimientoGroup(grupo: MovimientoAgrupado) {
        syncManager.deleteMovimientoGroup(grupo)
    }

    fun onSyncOverlayDismiss() {
        syncManager.dismissSyncCompleted()
    }

    fun clearStockValidationError() {
        _uiState.value = _uiState.value.copy(stockValidationError = null)
    }
}
