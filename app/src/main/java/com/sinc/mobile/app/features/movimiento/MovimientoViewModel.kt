package com.sinc.mobile.app.features.movimiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.MovimientoPendiente
import com.sinc.mobile.domain.use_case.SaveMovimientoLocalUseCase
import com.sinc.mobile.domain.use_case.SyncMovimientosPendientesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class MovimientoState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val syncSuccess: Boolean = false
)

@HiltViewModel
class MovimientoViewModel @Inject constructor(
    private val saveMovimientoLocalUseCase: SaveMovimientoLocalUseCase,
    private val syncMovimientosPendientesUseCase: SyncMovimientosPendientesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MovimientoState())
    val state: StateFlow<MovimientoState> = _state

    fun saveMovimiento(
        unidadProductivaId: Int,
        especieId: Int,
        categoriaId: Int,
        razaId: Int,
        cantidad: Int,
        motivoMovimientoId: Int,
        destinoTraslado: String?,
        observaciones: String?
    ) {
        viewModelScope.launch {
            _state.value = MovimientoState(isLoading = true)
            val movimiento = MovimientoPendiente(
                unidadProductivaId = unidadProductivaId,
                especieId = especieId,
                categoriaId = categoriaId,
                razaId = razaId,
                cantidad = cantidad,
                motivoMovimientoId = motivoMovimientoId,
                destinoTraslado = destinoTraslado,
                observaciones = observaciones,
                fechaRegistro = LocalDateTime.now() // Or should this be user-selectable?
            )
            val result = saveMovimientoLocalUseCase(movimiento)
            result.fold(
                onSuccess = { _state.value = MovimientoState(saveSuccess = true) },
                onFailure = { _state.value = MovimientoState(error = it.message) }
            )
        }
    }

    fun syncMovimientos() {
        viewModelScope.launch {
            _state.value = MovimientoState(isLoading = true)
            val result = syncMovimientosPendientesUseCase()
            result.fold(
                onSuccess = { _state.value = MovimientoState(syncSuccess = true) },
                onFailure = { _state.value = MovimientoState(error = it.message) }
            )
        }
    }
}
