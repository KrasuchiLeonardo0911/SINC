package com.sinc.mobile.data.repository

import com.sinc.mobile.data.local.dao.BitacoraDao
import com.sinc.mobile.data.mapper.toDomain
import com.sinc.mobile.data.mapper.toEntity
import com.sinc.mobile.data.network.api.BitacoraApiService
import com.sinc.mobile.data.network.dto.CreateBitacoraRequest
import com.sinc.mobile.data.network.dto.UpdateBitacoraRequest
import com.sinc.mobile.domain.model.Bitacora
import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.repository.BitacoraRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class BitacoraRepositoryImpl @Inject constructor(
    private val api: BitacoraApiService,
    private val dao: BitacoraDao
) : BitacoraRepository {

    override fun getBitacoras(): Flow<List<Bitacora>> {
        return dao.getAllBitacoras().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncBitacoras(): Result<Unit, Error> {
        return try {
            val response = api.getBitacoras()
            if (response.isSuccessful) {
                val bitacoras = response.body() ?: emptyList()
                dao.clearAndInsert(bitacoras.map { it.toEntity() })
                Result.Success(Unit)
            } else {
                Result.Failure(GenericError("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Failure(GenericError(e.message ?: "Error de red"))
        }
    }

    override suspend fun saveBitacora(fecha: LocalDate, contenido: String): Result<Bitacora, Error> {
        return try {
            val request = CreateBitacoraRequest(
                fecha = fecha.format(DateTimeFormatter.ISO_LOCAL_DATE),
                contenido = contenido
            )
            val response = api.createBitacora(request)
            if (response.isSuccessful && response.body() != null) {
                val bitacoraDto = response.body()!!
                val entity = bitacoraDto.toEntity()
                dao.insert(entity)
                Result.Success(entity.toDomain())
            } else {
                Result.Failure(GenericError("Error al guardar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Failure(GenericError(e.message ?: "Error de red"))
        }
    }

    override suspend fun updateBitacora(id: Int, contenido: String): Result<Bitacora, Error> {
        return try {
            val request = UpdateBitacoraRequest(contenido = contenido)
            val response = api.updateBitacora(id, request)
            if (response.isSuccessful && response.body() != null) {
                val bitacoraDto = response.body()!!
                val entity = bitacoraDto.toEntity()
                dao.insert(entity)
                Result.Success(entity.toDomain())
            } else {
                Result.Failure(GenericError("Error al actualizar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Failure(GenericError(e.message ?: "Error de red"))
        }
    }

    override suspend fun deleteBitacora(id: Int): Result<Unit, Error> {
        return try {
            val response = api.deleteBitacora(id)
            if (response.isSuccessful) {
                dao.deleteById(id)
                Result.Success(Unit)
            } else {
                Result.Failure(GenericError("Error al eliminar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Failure(GenericError(e.message ?: "Error de red"))
        }
    }
}
