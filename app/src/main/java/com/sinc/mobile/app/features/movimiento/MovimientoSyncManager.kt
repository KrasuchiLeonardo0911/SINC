package com.sinc.mobile.app.features.movimiento

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.sinc.mobile.domain.model.MovimientoPendiente
import com.sinc.mobile.domain.use_case.DeleteMovimientoLocalUseCase
import com.sinc.mobile.domain.use_case.GetMovimientosPendientesUseCase
import com.sinc.mobile.domain.use_case.SyncMovimientosPendientesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private data class MovimientoGroupKey(
    val unidadProductivaId: Int,
    val especieId: Int,
    val categoriaId: Int,
    val razaId: Int,
    val motivoMovimientoId: Int
)

data class MovimientoSyncState(
    val movimientosAgrupados: List<MovimientoAgrupado> = emptyList(),
    val isSyncing: Boolean = false,
    val syncError: String? = null,
    val syncSuccess: Boolean = false,
)

class MovimientoSyncManager(
    private val getMovimientosPendientesUseCase: GetMovimientosPendientesUseCase,
    private val syncMovimientosPendientesUseCase: SyncMovimientosPendientesUseCase,
    private val deleteMovimientoLocalUseCase: DeleteMovimientoLocalUseCase,
    private val scope: CoroutineScope
) {
    private val _syncState = mutableStateOf(MovimientoSyncState())
    val syncState: State<MovimientoSyncState> = _syncState

    init {
        loadAndGroupMovimientos()
    }

    private fun loadAndGroupMovimientos() {
        scope.launch {
            getMovimientosPendientesUseCase().collect { movimientos ->
                val agrupados = movimientos
                    .groupBy {
                        MovimientoGroupKey(
                            unidadProductivaId = it.unidadProductivaId,
                            especieId = it.especieId,
                            categoriaId = it.categoriaId,
                            razaId = it.razaId,
                            motivoMovimientoId = it.motivoMovimientoId
                        )
                    }
                    .map { (_, group) ->
                        val first = group.first()
                        MovimientoAgrupado(
                            unidadProductivaId = first.unidadProductivaId,
                            especieId = first.especieId,
                            categoriaId = first.categoriaId,
                            razaId = first.razaId,
                            motivoMovimientoId = first.motivoMovimientoId,
                            cantidadTotal = group.sumOf { it.cantidad },
                            originales = group
                        )
                    }
                _syncState.value = _syncState.value.copy(movimientosAgrupados = agrupados)
            }
        }
    }

    fun deleteMovimientoGroup(grupo: MovimientoAgrupado) {
        scope.launch {
            grupo.originales.forEach { movimiento ->
                deleteMovimientoLocalUseCase(movimiento).onFailure {
                    // Optionally handle error for single deletion failure
                }
            }
        }
    }

    fun syncMovements() {
        scope.launch {
            _syncState.value = _syncState.value.copy(isSyncing = true, syncError = null, syncSuccess = false)
            syncMovimientosPendientesUseCase().onSuccess {
                _syncState.value = _syncState.value.copy(isSyncing = false, syncSuccess = true)
                kotlinx.coroutines.delay(2000L)
                _syncState.value = _syncState.value.copy(syncSuccess = false)
            }.onFailure {
                android.util.Log.e("SyncError", "Fallo al sincronizar movimientos", it)
                _syncState.value = _syncState.value.copy(isSyncing = false, syncError = it.message ?: "Error al sincronizar")
                kotlinx.coroutines.delay(3000L)
                _syncState.value = _syncState.value.copy(syncError = null)
            }
        }
    }
}
