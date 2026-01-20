package com.sinc.mobile.app.features.movimiento

import com.sinc.mobile.domain.use_case.DeleteMovimientoLocalUseCase
import com.sinc.mobile.domain.use_case.GetMovimientosPendientesUseCase
import com.sinc.mobile.domain.use_case.SyncMovimientosLocalesUseCase
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
    val syncCompleted: Boolean = false, // New state for success animation
    val syncError: String? = null,
)

class MovimientoSyncManager(
    private val getMovimientosPendientesUseCase: GetMovimientosPendientesUseCase,
    private val syncMovimientosLocalesUseCase: SyncMovimientosLocalesUseCase,
    private val deleteMovimientoLocalUseCase: DeleteMovimientoLocalUseCase,
    private val syncStockUseCase: SyncStockUseCase,
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

    fun syncMovements() {
        scope.launch {
            _syncState.value = _syncState.value.copy(isSyncing = true, syncError = null, syncCompleted = false)
            val startTime = System.currentTimeMillis()

            val result = syncMovimientosLocalesUseCase()
            if (result is Result.Success) {
                val syncedMovements = result.data
                val duration = System.currentTimeMillis() - startTime
                if (duration < 1000) {
                    delay(1000 - duration) // Ensure spinner is visible for at least 1s
                }
                // Show success animation
                _syncState.value = _syncState.value.copy(isSyncing = false, syncCompleted = true)

                // Delete the local items now, which will trigger the UI update
                syncedMovements.forEach {
                    deleteMovimientoLocalUseCase(it)
                }

                // Now, trigger a refresh of the total stock
                syncStockUseCase()
            } else if (result is Result.Failure) {
                val error = result.error
                val duration = System.currentTimeMillis() - startTime
                if (duration < 1000) {
                    delay(1000 - duration) // Ensure spinner is visible for at least 1s
                }
                _syncState.value = _syncState.value.copy(isSyncing = false, syncError = error.message)
                delay(3000L) // Keep error message visible
                _syncState.value = _syncState.value.copy(syncError = null)
            }
        }
    }

    fun dismissSyncCompleted() {
        _syncState.value = _syncState.value.copy(syncCompleted = false)
    }
}
