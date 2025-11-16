package com.sinc.mobile.data.repository

import com.sinc.mobile.data.network.api.UnidadProductivaApiService
import com.sinc.mobile.data.network.dto.request.CreateUnidadProductivaRequest
import com.sinc.mobile.data.network.dto.response.UnidadProductivaDto
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.model.CreateUnidadProductivaData
import com.sinc.mobile.domain.repository.UnidadProductivaRepository
import java.io.IOException
import javax.inject.Inject

import com.sinc.mobile.data.local.dao.UnidadProductivaDao
import com.sinc.mobile.data.local.entities.UnidadProductivaEntity
import com.sinc.mobile.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UnidadProductivaRepositoryImpl @Inject constructor(
    private val unidadProductivaApiService: UnidadProductivaApiService,
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
            val response = unidadProductivaApiService.getUnidadesProductivas()
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

    override suspend fun createUnidadProductiva(data: CreateUnidadProductivaData): Result<UnidadProductiva> {
        val authToken = sessionManager.getAuthToken()
            ?: return Result.failure(Exception("No hay token de autenticación disponible para crear unidades productivas."))

        val request = CreateUnidadProductivaRequest(
            nombre = data.nombre,
            identificadorLocal = data.identificadorLocal,
            superficie = data.superficie,
            latitud = data.latitud,
            longitud = data.longitud,
            municipioId = data.municipioId,
            condicionTenenciaId = data.condicionTenenciaId,
            fuenteAguaId = data.fuenteAguaId,
            tipoSueloId = data.tipoSueloId,
            tipoPastoId = data.tipoPastoId
        )

        return try {
            val response = unidadProductivaApiService.createUnidadProductiva(request)
            if (response.isSuccessful) {
                val dto = response.body()
                if (dto != null) {
                    // Opcional: Insertar la nueva UP en la base de datos local inmediatamente
                    unidadProductivaDao.insertUnidadProductiva(dto.toEntity())
                    Result.success(dto.toDomain())
                } else {
                    Result.failure(Exception("El cuerpo de la respuesta de creación de unidad productiva es nulo"))
                }
            } else {
                Result.failure(Exception("Error de API al crear unidad productiva: ${response.code()} - ${response.message()}"))
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
        identificadorLocal = this.identificadorLocal,
        superficie = this.superficie,
        latitud = this.latitud?.toDoubleOrNull(),
        longitud = this.longitud?.toDoubleOrNull(),
        municipioId = this.municipioId,
        condicionTenenciaId = this.condicionTenenciaId,
        fuenteAguaId = this.fuenteAguaId,
        tipoSueloId = this.tipoSueloId,
        tipoPastoId = this.tipoPastoId
    )
}

private fun UnidadProductivaDto.toDomain(): UnidadProductiva {
    return UnidadProductiva(
        id = this.id,
        nombre = this.nombre,
        identificadorLocal = this.identificadorLocal,
        superficie = this.superficie,
        latitud = this.latitud?.toDoubleOrNull(),
        longitud = this.longitud?.toDoubleOrNull(),
        municipioId = this.municipioId,
        condicionTenenciaId = this.condicionTenenciaId,
        fuenteAguaId = this.fuenteAguaId,
        tipoSueloId = this.tipoSueloId,
        tipoPastoId = this.tipoPastoId
    )
}

private fun UnidadProductivaEntity.toDomain(): UnidadProductiva {
    return UnidadProductiva(
        id = this.id,
        nombre = this.nombre,
        identificadorLocal = this.identificadorLocal,
        superficie = this.superficie,
        latitud = this.latitud,
        longitud = this.longitud,
        municipioId = this.municipioId,
        condicionTenenciaId = this.condicionTenenciaId,
        fuenteAguaId = this.fuenteAguaId,
        tipoSueloId = this.tipoSueloId,
        tipoPastoId = this.tipoPastoId
    )
}
