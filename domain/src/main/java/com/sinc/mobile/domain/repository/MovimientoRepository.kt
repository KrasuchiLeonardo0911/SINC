package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.Movimiento
import com.sinc.mobile.domain.model.MovimientoPendiente
import kotlinx.coroutines.flow.Flow
import com.sinc.mobile.domain.util.Result as DomainResult
import com.sinc.mobile.domain.util.Error as DomainError

interface MovimientoRepository {
    suspend fun saveMovimientoLocal(movimiento: MovimientoPendiente): DomainResult<Unit, DomainError>
    fun getMovimientosPendientes(): Flow<List<MovimientoPendiente>>
    suspend fun deleteMovimientoLocal(movimiento: MovimientoPendiente): DomainResult<Unit, DomainError>
    suspend fun syncMovimientosPendientesToServer(): DomainResult<List<MovimientoPendiente>, DomainError>
}
