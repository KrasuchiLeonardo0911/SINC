package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.Movimiento

interface MovimientoRepository {
    suspend fun saveMovimiento(movimiento: Movimiento): Result<Unit>
}
