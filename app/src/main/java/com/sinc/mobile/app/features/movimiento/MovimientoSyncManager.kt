package com.sinc.mobile.app.features.movimiento

import com.sinc.mobile.domain.repository.MovimientoHistorialRepository
import com.sinc.mobile.domain.use_case.DeleteMovimientoLocalUseCase
import com.sinc.mobile.domain.use_case.GetMovimientosPendientesUseCase
import com.sinc.mobile.domain.use_case.SyncMovimientosHistorialUseCase
import com.sinc.mobile.domain.use_case.SyncStockUseCase
import com.sinc.mobile.domain.util.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

// This is the key for grouping movements. Made internal to this file as it's an implementation detail.
private data class MovimientoGroupKey(
    val unidadProductivaId: Int,
    val especieId: Int,
    val categoriaId: Int,
    val razaId: Int,
    val motivoMovimientoId: Int,
    val destinoTraslado: String?
)

data class MovimientoSyncState(
    val movimientosAgrupados: List<MovimientoAgrupado> = emptyList(),
    val isSyncing: Boolean = false,
    val syncCompleted: Boolean = false,
    val syncError: String? = null,
)

class MovimientoSyncManager(
    private val getMovimientosPendientesUseCase: GetMovimientosPendientesUseCase,
    private val deleteMovimientoLocalUseCase: DeleteMovimientoLocalUseCase,
    private val historialRepository: MovimientoHistorialRepository,
    private val syncStockUseCase: SyncStockUseCase,
    private val syncMovimientosHistorialUseCase: SyncMovimientosHistorialUseCase,
    private val scope: CoroutineScope
) {
    private val _syncState = MutableStateFlow(MovimientoSyncState())
    val syncState: StateFlow<MovimientoSyncState> = _syncState

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
                            motivoMovimientoId = it.motivoMovimientoId,
                            destinoTraslado = it.destinoTraslado
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
                            destinoTraslado = first.destinoTraslado,
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
                deleteMovimientoLocalUseCase(movimiento)
            }
        }
    }

    fun triggerBackgroundSync() {
        scope.launch {
            // Silently try to sync history
            historialRepository.syncUnsyncedMovements()
            // After attempting sync, we should ideally refresh stock from server 
            // to make sure we are in sync with reality, BUT our local calculation 
            // already covers it. Refreshing history from server is good though.
            syncMovimientosHistorialUseCase()
            syncStockUseCase()
        }
    }

    fun syncMovements() { // Still useful for manual retry or troubleshooting
        scope.launch {
            _syncState.value = _syncState.value.copy(isSyncing = true, syncError = null, syncCompleted = false)
            val startTime = System.currentTimeMillis()

            val result = historialRepository.syncUnsyncedMovements()
            if (result is Result.Success) {
                val duration = System.currentTimeMillis() - startTime
                if (duration < 1000) {
                    delay(1000 - duration)
                }
                _syncState.value = _syncState.value.copy(isSyncing = false, syncCompleted = true)

                // Refresh total stock AND movement history from server
                syncStockUseCase()
                syncMovimientosHistorialUseCase()
            } else if (result is Result.Failure) {
                val error = result.error
                val duration = System.currentTimeMillis() - startTime
                if (duration < 1000) {
                    delay(1000 - duration)
                }
                _syncState.value = _syncState.value.copy(isSyncing = false, syncError = error.message)
                delay(3000L)
                _syncState.value = _syncState.value.copy(syncError = null)
            }
        }
    }

    fun dismissSyncCompleted() {
        _syncState.value = _syncState.value.copy(syncCompleted = false)
    }
}
