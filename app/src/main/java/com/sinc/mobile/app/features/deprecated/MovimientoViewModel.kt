/*
package com.sinc.mobile.app.features.movimiento

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.use_case.GetUnidadesProductivasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.use_case.GetCatalogosUseCase
import java.time.LocalDateTime
import com.sinc.mobile.domain.model.MovimientoPendiente
import com.sinc.mobile.domain.use_case.GetMovimientosPendientesUseCase
import com.sinc.mobile.domain.use_case.SaveMovimientoLocalUseCase
import com.sinc.mobile.domain.use_case.SyncMovimientosPendientesUseCase
import com.sinc.mobile.domain.use_case.DeleteMovimientoLocalUseCase
import kotlinx.coroutines.flow.collectLatest

// Data class to represent a grouped movement for the UI
data class MovimientoAgrupado(
    val unidadProductivaId: Int,
    val especieId: Int,
    val categoriaId: Int,
    val razaId: Int,
    val motivoMovimientoId: Int,
    val cantidadTotal: Int,
    val originales: List<MovimientoPendiente> // Keep original items for deletion
)

data class MovimientoState(
    // General State
    val isLoading: Boolean = true,
    val error: String? = null,

    // Data
    val unidades: List<UnidadProductiva> = emptyList(),
    val catalogos: Catalogos? = null,

    // Step Selections
    val selectedUnidad: UnidadProductiva? = null,
    val selectedAction: String? = null, // "alta" or "baja"
    val isUnidadSelectedLoading: Boolean = false,
    val showShimmer: Boolean = false, // New state for shimmer control


    // UI State
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isFormLoading: Boolean = false
)

@HiltViewModel
class MovimientoViewModel @Inject constructor(
    private val getUnidadesProductivasUseCase: GetUnidadesProductivasUseCase,
    private val getCatalogosUseCase: GetCatalogosUseCase,
    private val saveMovimientoLocalUseCase: SaveMovimientoLocalUseCase,
    getMovimientosPendientesUseCase: GetMovimientosPendientesUseCase,
    syncMovimientosPendientesUseCase: SyncMovimientosPendientesUseCase,
    deleteMovimientoLocalUseCase: DeleteMovimientoLocalUseCase
) : ViewModel() {

    private val _state = mutableStateOf(MovimientoState())
    val state: State<MovimientoState> = _state

    // Managers for specific logic
    var formManager: MovimientoFormManager? = null
        private set
    val syncManager: MovimientoSyncManager

    init {
        syncManager = MovimientoSyncManager(
            getMovimientosPendientesUseCase,
            syncMovimientosPendientesUseCase,
            deleteMovimientoLocalUseCase,
            viewModelScope
        )
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            // Catalogos se cargan una vez
            getCatalogosUseCase().collectLatest { catalogos ->
                _state.value = _state.value.copy(catalogos = catalogos)
            }
        }
        viewModelScope.launch {
            // Unidades productivas se cargan una vez
            getUnidadesProductivasUseCase().collectLatest { unidades ->
                _state.value = _state.value.copy(unidades = unidades, isLoading = false)
            }
        }
    }

    fun onUnidadSelected(unidad: UnidadProductiva) {
        viewModelScope.launch {
            if (state.value.selectedUnidad == unidad && formManager != null) return@launch

            // 1. Show static skeleton
            _state.value = _state.value.copy(
                isUnidadSelectedLoading = true,
                showShimmer = false,
                selectedUnidad = unidad,
                selectedAction = null,
                saveError = null
            )

            // 2. Wait for slide-in animation to finish
            kotlinx.coroutines.delay(300L)

            // 3. Show shimmer animation
            _state.value = _state.value.copy(showShimmer = true)

            // 4. Wait for shimmer to be visible for a moment
            kotlinx.coroutines.delay(400L)

            // 5. Create the form manager and show content
            formManager = MovimientoFormManager(state.value.catalogos)
            _state.value = _state.value.copy(isUnidadSelectedLoading = false)
        }
    }

    fun dismissForm() {
        _state.value = _state.value.copy(selectedAction = null)
        formManager = null
    }

    fun saveMovement() {
        val formState = formManager?.formState?.value ?: return
        if (!formState.isFormValid) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, saveError = null)
            val s = state.value
            val movimiento = MovimientoPendiente(
                id = 0, // Room will auto-generate
                unidadProductivaId = s.selectedUnidad!!.id,
                especieId = formState.selectedEspecie!!.id,
                categoriaId = formState.selectedCategoria!!.id,
                razaId = formState.selectedRaza!!.id,
                cantidad = formState.cantidad.toInt(),
                motivoMovimientoId = formState.selectedMotivo!!.id,
                destinoTraslado = if (formState.selectedMotivo?.nombre?.contains("Traslado", ignoreCase = true) == true ||
                    formState.selectedMotivo?.nombre?.contains("Venta", ignoreCase = true) == true ||
                    formState.selectedMotivo?.nombre?.contains("Compra", ignoreCase = true) == true) formState.destino else null,
                observaciones = null,
                fechaRegistro = LocalDateTime.now(),
                sincronizado = false
            )
            saveMovimientoLocalUseCase(movimiento).onSuccess {
                _state.value = _state.value.copy(
                    isSaving = false,
                    selectedAction = null, // Oculta el formulario
                    saveError = null
                )
                formManager = null // Resetea el form manager
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false, saveError = it.message ?: "Error al guardar movimiento")
            }
        }
    }
}
*/