package com.sinc.mobile.app.features.movimiento

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.app.features.movimiento.MovimientoFormManager
import com.sinc.mobile.app.features.movimiento.MovimientoSyncManager
import com.sinc.mobile.app.features.movimiento.MovimientoSyncState
import com.sinc.mobile.domain.model.Catalogos
import com.sinc.mobile.domain.model.MovimientoPendiente
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.use_case.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class MovimientoStepperState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedUnidad: UnidadProductiva? = null,
    val formManager: MovimientoFormManager? = null,
    val syncState: MovimientoSyncState = MovimientoSyncState(),
)

@OptIn(ExperimentalFoundationApi::class)
@HiltViewModel
class MovimientoStepperViewModel @Inject constructor(
    private val getUnidadesProductivasUseCase: GetUnidadesProductivasUseCase,
    private val getCatalogosUseCase: GetCatalogosUseCase,
    private val saveMovimientoLocalUseCase: SaveMovimientoLocalUseCase,
    getMovimientosPendientesUseCase: GetMovimientosPendientesUseCase,
    private val syncMovimientosPendientesUseCase: SyncMovimientosPendientesUseCase,
    private val deleteMovimientoLocalUseCase: DeleteMovimientoLocalUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovimientoStepperState())
    val uiState = _uiState.asStateFlow()

    private val syncManager: MovimientoSyncManager

    private var catalogos: Catalogos? = null
    private val unidadId: String? = savedStateHandle.get("unidadId")

    init {
        syncManager = MovimientoSyncManager(
            getMovimientosPendientesUseCase,
            syncMovimientosPendientesUseCase,
            deleteMovimientoLocalUseCase,
            viewModelScope
        )

        // Observe the sync state from the manager
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
            combine(
                getUnidadesProductivasUseCase(),
                getCatalogosUseCase()
            ) { unidades, catalogosData ->
                val selectedUnidad = unidades.find { it.id.toString() == unidadId }
                catalogos = catalogosData // Cache catalogos for form resets

                // Update state in one go
                _uiState.value = _uiState.value.copy(
                    selectedUnidad = selectedUnidad,
                    formManager = MovimientoFormManager(catalogosData),
                    isLoading = false // As soon as we have a value from both flows, we're ready
                )
            }.launchIn(viewModelScope) // The combine flow is launched and will keep running
        }
    }

    fun onAddToList(pagerState: PagerState) {
        val formState = _uiState.value.formManager?.formState?.value ?: return
        if (!formState.isFormValid) {
            return
        }

        viewModelScope.launch {
            val movimiento = MovimientoPendiente(
                id = 0,
                unidadProductivaId = _uiState.value.selectedUnidad!!.id,
                especieId = formState.selectedEspecie!!.id,
                categoriaId = formState.selectedCategoria!!.id,
                razaId = formState.selectedRaza!!.id,
                cantidad = formState.cantidad.toIntOrNull() ?: 0,
                motivoMovimientoId = formState.selectedMotivo!!.id,
                destinoTraslado = null,
                observaciones = null,
                fechaRegistro = LocalDateTime.now(),
                sincronizado = false
            )

            saveMovimientoLocalUseCase(movimiento).onSuccess {
                // Reset form by creating a new manager
                _uiState.value = _uiState.value.copy(formManager = MovimientoFormManager(catalogos))
                pagerState.animateScrollToPage(1)
            }.onFailure {
                // TODO: Show error
            }
        }
    }

    fun onSync() {
        syncManager.syncMovements()
    }

    fun onDelete(movimiento: MovimientoPendiente) {
        viewModelScope.launch {
            deleteMovimientoLocalUseCase(movimiento)
        }
    }
}
