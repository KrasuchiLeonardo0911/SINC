package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.DeclaracionVenta
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.model.GenericError as Error
import kotlinx.coroutines.flow.Flow

interface VentasRepository {
    fun getDeclaraciones(): Flow<List<DeclaracionVenta>>
    suspend fun syncDeclaraciones(): Result<Unit, Error>
    suspend fun createDeclaracion(
        unidadProductivaId: Int,
        especieId: Int,
        razaId: Int,
        categoriaAnimalId: Int,
        cantidad: Int,
        observaciones: String?,
        pesoAproximadoKg: Float?
    ): Result<Unit, Error>
    
    suspend fun getPendingQuantity(
        unidadProductivaId: Int,
        especieId: Int,
        razaId: Int,
        categoriaAnimalId: Int
    ): Int
}
