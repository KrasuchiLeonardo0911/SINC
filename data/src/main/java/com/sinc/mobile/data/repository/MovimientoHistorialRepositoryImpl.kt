package com.sinc.mobile.data.repository

import com.sinc.mobile.data.local.dao.MovimientoHistorialDao
import com.sinc.mobile.data.mapper.toDomain
import com.sinc.mobile.data.mapper.toEntity
import com.sinc.mobile.data.network.api.HistorialMovimientosApiService
import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.model.MovimientoHistorial
import com.sinc.mobile.domain.repository.MovimientoHistorialRepository
import com.sinc.mobile.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MovimientoHistorialRepositoryImpl @Inject constructor(
    private val apiService: HistorialMovimientosApiService,
    private val dao: MovimientoHistorialDao
) : MovimientoHistorialRepository {

    override fun getMovimientos(): Flow<List<MovimientoHistorial>> {
        return dao.getAllMovimientos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncMovimientos(): Result<Unit, GenericError> {
        return try {
            val response = apiService.getHistorialMovimientos()
            if (response.isSuccessful) {
                val dtos = response.body()
                if (dtos != null) {
                    dao.clearAndInsert(dtos.map { it.toEntity() })
                    Result.Success(Unit)
                } else {
                    Result.Failure(GenericError("Response body is null"))
                }
            } else {
                Result.Failure(GenericError("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Failure(GenericError("Network Error: ${e.message ?: "Unknown"}"))
        }
    }
}
