package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.Movimiento
import com.sinc.mobile.domain.model.MovimientoPendiente
import kotlinx.coroutines.flow.Flow

interface MovimientoRepository {
    suspend fun saveMovimientoLocal(movimiento: MovimientoPendiente): Result<Unit>
    fun getMovimientosPendientes(): Flow<List<MovimientoPendiente>>
    suspend fun syncMovimientosPendientes(): Result<Unit>
    suspend fun saveMovimiento(movimiento: Movimiento): Result<Unit>
}
