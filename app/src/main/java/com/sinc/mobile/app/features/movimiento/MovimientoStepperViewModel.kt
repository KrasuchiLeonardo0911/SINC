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
    private val syncMovimientosLocalesUseCase: SyncMovimientosLocalesUseCase, // Updated UseCase
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
            syncMovimientosLocalesUseCase, // Updated parameter
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
        _uiState.update { it.copy(isLoading = true) }

        // Combine the flows that provide data to the screen
        val combinedDataFlow = combine(
            getUnidadesProductivasUseCase(),
            getMovimientoCatalogosUseCase(),
            stockRepository.getStock()
        ) { unidades, catalogos, stock ->
            // Package them into a data class for clarity
            Triple(unidades, catalogos, stock)
        }

        viewModelScope.launch {
            // Collect the flow. This will run for the lifetime of the ViewModel.
            combinedDataFlow.collect { (unidades, catalogosData, stockData) ->
                // This block will be executed each time the list of unidades,
                // catalogos, or stock changes in the database.

                val currentUiState = _uiState.value

                // On the very first data emission, determine the initially selected unit if an ID was passed via navigation.
                // On subsequent emissions, we respect the user's current selection.
                val selectedUnidad = if (currentUiState.isLoading) { // Use isLoading as a proxy for "first emission"
                    unidades.find { it.id.toString() == unidadId }
                } else {
                    // If a unit is already selected, refresh its instance from the new list.
                    // If the user hasn't selected one, keep it as null.
                    currentUiState.selectedUnidad?.let { current -> unidades.find { it.id == current.id } }
                }

                // Update the local catalogos cache
                catalogos = catalogosData

                // Update the entire UI state
                _uiState.update {
                    it.copy(
                        isLoading = false, // Turn off loading after the first data emission
                        unidades = unidades,
                        catalogos = catalogosData,
                        stock = stockData,
                        selectedUnidad = selectedUnidad,
                        // Re-initialize formManager only on first load to not lose user input
                        formManager = if (currentUiState.isLoading) MovimientoFormManager(catalogosData) else it.formManager
                    )
                }
            }
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
                
                // saveMovimientoLocalUseCase uses result.onSuccess {}
                val result = saveMovimientoLocalUseCase(movimiento)
                if (result is com.sinc.mobile.domain.util.Result.Success) {
                     _uiState.value = _uiState.value.copy(formManager = MovimientoFormManager(catalogos))
                    _navigateToPage.emit(1)
                } else if (result is com.sinc.mobile.domain.util.Result.Failure) {
                    // Handle failure
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