package com.sinc.mobile.data.repository

import com.sinc.mobile.data.local.dao.DeclaracionVentaDao
import com.sinc.mobile.data.mapper.toDomain
import com.sinc.mobile.data.mapper.toEntity
import com.sinc.mobile.data.network.api.VentasApiService
import com.sinc.mobile.data.network.dto.request.CreateDeclaracionVentaRequest
import com.sinc.mobile.domain.repository.VentasRepository
import com.sinc.mobile.domain.model.DeclaracionVenta
import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.model.GenericError as Error
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class VentasRepositoryImpl @Inject constructor(
    private val api: VentasApiService,
    private val dao: DeclaracionVentaDao
) : VentasRepository {

    override fun getDeclaraciones(): Flow<List<DeclaracionVenta>> {
        return dao.getAllDeclaraciones().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncDeclaraciones(): Result<Unit, Error> {
        return try {
            val response = api.getDeclaracionesVenta()
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!
                dao.clearAndInsert(dtos.map { it.toEntity() })
                Result.Success(Unit)
            } else {
                Result.Failure(GenericError("Error de red o respuesta vacía del servidor."))
            }
        } catch (e: IOException) {
            Result.Failure(GenericError("Error de conexión: Asegúrate de tener acceso a internet."))
        } catch (e: HttpException) {
            Result.Failure(GenericError("Error de red: ${e.message()}"))
        }
    }

    override suspend fun createDeclaracion(
        unidadProductivaId: Int,
        especieId: Int,
        razaId: Int,
        categoriaAnimalId: Int,
        cantidad: Int,
        observaciones: String?
    ): Result<Unit, Error> {
        return try {
            val request = CreateDeclaracionVentaRequest(
                unidadProductivaId = unidadProductivaId,
                especieId = especieId,
                razaId = razaId,
                categoriaAnimalId = categoriaAnimalId,
                cantidad = cantidad,
                observaciones = observaciones
            )
            val response = api.createDeclaracionVenta(request)
            if (response.isSuccessful) {
                syncDeclaraciones()
                Result.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                if (response.code() == 422) {
                     Result.Failure(GenericError("Validación fallida: $errorBody"))
                } else {
                     Result.Failure(GenericError("Error servidor (${response.code()}): $errorBody"))
                }
            }
        } catch (e: Exception) {
            Result.Failure(GenericError("Ocurrió un error inesperado al crear la declaración. ${e.message}"))
        }
    }
    override suspend fun getPendingQuantity(
        unidadProductivaId: Int,
        especieId: Int,
        razaId: Int,
        categoriaAnimalId: Int
    ): Int {
        return dao.getSumPendientes(unidadProductivaId, especieId, razaId, categoriaAnimalId) ?: 0
    }
}
