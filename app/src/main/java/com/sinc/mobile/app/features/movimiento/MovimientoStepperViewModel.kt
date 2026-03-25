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
import com.sinc.mobile.domain.repository.MovimientoHistorialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    val unidades: List<UnidadProductiva> = emptyList(),
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
    private val deleteMovimientoLocalUseCase: DeleteMovimientoLocalUseCase,
    private val getEffectiveStockUseCase: GetEffectiveStockUseCase,
    private val confirmMovimientosUseCase: ConfirmMovimientosUseCase,
    private val syncStockUseCase: SyncStockUseCase,
    private val syncMovimientosHistorialUseCase: SyncMovimientosHistorialUseCase,
    private val historialRepository: MovimientoHistorialRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovimientoStepperState())
    val uiState = _uiState.asStateFlow()

    private val _navigateToPage = MutableSharedFlow<Int>()
    val navigateToPage = _navigateToPage.asSharedFlow()

    private val syncManager: MovimientoSyncManager

    private var catalogos: Catalogos? = null
    private val unidadId: String? = savedStateHandle.get("unidadId")
    private val preSelectedEspecieId: Int = savedStateHandle.get<Int>("especieId") ?: -1
    private val preSelectedRazaId: Int = savedStateHandle.get<Int>("razaId") ?: -1
    private val preSelectedCategoriaId: Int = savedStateHandle.get<Int>("categoriaId") ?: -1

    init {
        syncManager = MovimientoSyncManager(
            getMovimientosPendientesUseCase,
            deleteMovimientoLocalUseCase,
            historialRepository, // Pass this to manage background sync
            syncStockUseCase,
            syncMovimientosHistorialUseCase,
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

        val combinedDataFlow = combine(
            getUnidadesProductivasUseCase(),
            getMovimientoCatalogosUseCase(),
            getEffectiveStockUseCase()
        ) { unidades, catalogos, stock ->
            Triple(unidades, catalogos, stock)
        }

        viewModelScope.launch {
            combinedDataFlow.collect { (unidades, catalogosData, stockData) ->
                val currentUiState = _uiState.value
                val selectedUnidad = if (currentUiState.isLoading) {
                    unidades.find { it.id.toString() == unidadId }
                } else {
                    currentUiState.selectedUnidad?.let { current -> unidades.find { unit -> unit.id == current.id } }
                }

                catalogos = catalogosData

                val isFirstEmission = currentUiState.isLoading
                val newFormManager = if (isFirstEmission) {
                    val manager = MovimientoFormManager(catalogosData)
                    if (preSelectedEspecieId != -1) {
                        catalogosData.especies.find { esp -> esp.id == preSelectedEspecieId }?.let { especie ->
                            manager.onEspecieSelected(especie)
                        }
                    }
                    if (preSelectedCategoriaId != -1) {
                        catalogosData.categorias.find { cat -> cat.id == preSelectedCategoriaId }?.let { categoria ->
                            manager.onCategoriaSelected(categoria)
                        }
                    }
                    if (preSelectedRazaId != -1) {
                        catalogosData.razas.find { rz -> rz.id == preSelectedRazaId }?.let { raza ->
                            manager.onRazaSelected(raza)
                        }
                    }
                    manager
                } else {
                    currentUiState.formManager
                }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        unidades = unidades,
                        catalogos = catalogosData,
                        stock = stockData,
                        selectedUnidad = selectedUnidad,
                        formManager = newFormManager
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

        val stock = _uiState.value.stock ?: run {
            _uiState.value = _uiState.value.copy(stockValidationError = "No se pudo verificar el stock. Intente de nuevo.")
            return
        }

        val especieId = currentFormState.selectedEspecie!!.id
        val categoriaId = currentFormState.selectedCategoria!!.id
        val razaId = currentFormState.selectedRaza!!.id
        val cantidadADescontar = currentFormState.cantidad.toIntOrNull() ?: 0

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

        // In the new logic, stockActual already considers unsynced history records.
        // We only need to consider the CURRENT draft list (movimientosAgrupados)
        val bajasEnBorrador = _uiState.value.syncState.movimientosAgrupados
            .filter {
                it.especieId == especieId &&
                        it.categoriaId == categoriaId &&
                        it.razaId == razaId &&
                        it.unidadProductivaId == unidad.id &&
                        _uiState.value.catalogos?.motivosMovimiento?.find { m -> m.id == it.motivoMovimientoId }?.tipo?.equals("baja", ignoreCase = true) == true
            }
            .sumOf { it.cantidadTotal }

        val stockDisponible = stockActual - bajasEnBorrador
        if (cantidadADescontar > stockDisponible) {
            val errorMessage = "Stock insuficiente. Disponible: $stockDisponible (Actual: $stockActual, En borrador: $bajasEnBorrador)"
            _uiState.value = _uiState.value.copy(stockValidationError = errorMessage)
            return
        }
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
                
                val result = saveMovimientoLocalUseCase(movimiento)
                if (result is com.sinc.mobile.domain.util.Result.Success) {
                     _uiState.value = _uiState.value.copy(formManager = MovimientoFormManager(catalogos))
                    _navigateToPage.emit(1)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun onSave() { // Renamed from onSync
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            // Artificial delay to improve UX and show the loading screen
            kotlinx.coroutines.delay(1500)
            
            val result = confirmMovimientosUseCase()
            _uiState.update { it.copy(isSaving = false) }
            
            if (result is com.sinc.mobile.domain.util.Result.Success) {
                // After saving locally, trigger background sync
                syncManager.triggerBackgroundSync()
                
                // Show success banner
                com.sinc.mobile.app.ui.components.BannerManager.show(
                    "Movimientos guardados exitosamente", 
                    com.sinc.mobile.app.ui.components.BannerType.SUCCESS
                )
                
                // Navigation back to form
                _navigateToPage.emit(0)
            } else if (result is com.sinc.mobile.domain.util.Result.Failure) {
                _uiState.update { it.copy(error = result.error.message) }
            }
        }
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
