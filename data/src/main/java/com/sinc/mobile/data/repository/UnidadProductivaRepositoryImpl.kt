package com.sinc.mobile.data.repository

import com.sinc.mobile.data.network.api.AuthApiService
import com.sinc.mobile.data.network.dto.UnidadProductivaDto
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.repository.UnidadProductivaRepository
import java.io.IOException
import javax.inject.Inject

import com.sinc.mobile.data.local.dao.UnidadProductivaDao
import com.sinc.mobile.data.local.entities.UnidadProductivaEntity
import com.sinc.mobile.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UnidadProductivaRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val sessionManager: SessionManager,
    private val unidadProductivaDao: UnidadProductivaDao
) : UnidadProductivaRepository {

    override fun getUnidadesProductivas(): Flow<List<UnidadProductiva>> {
        return unidadProductivaDao.getAllUnidadesProductivas().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncUnidadesProductivas(): Result<Unit> {
        val authToken = sessionManager.getAuthToken()
            ?: return Result.failure(Exception("No hay token de autenticación disponible para sincronizar unidades productivas."))

        return try {
            val response = apiService.getUnidadesProductivas("Bearer $authToken")
            if (response.isSuccessful) {
                val dtos = response.body()
                if (dtos != null) {
                    unidadProductivaDao.clearAndInsert(dtos.map { it.toEntity() })
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("El cuerpo de la respuesta de unidades productivas es nulo"))
                }
            } else {
                // Reutilizar el manejo de errores si es posible, o crear uno específico
                Result.failure(Exception("Error de API al sincronizar unidades productivas: ${response.code()} - ${response.message()}"))
            }
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun UnidadProductivaDto.toEntity(): UnidadProductivaEntity {
    return UnidadProductivaEntity(
        id = this.id,
        nombre = this.nombre,
        latitud = this.latitud?.toDoubleOrNull(),
        longitud = this.longitud?.toDoubleOrNull(),
        municipio_id = null, // No disponible en el DTO
        paraje_id = null // No disponible en el DTO
    )
}

private fun UnidadProductivaEntity.toDomain(): UnidadProductiva {
    return UnidadProductiva(
        id = this.id,
        nombre = this.nombre ?: ""
    )
}
