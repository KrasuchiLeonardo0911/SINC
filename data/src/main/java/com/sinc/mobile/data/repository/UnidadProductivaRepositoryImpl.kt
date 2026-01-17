package com.sinc.mobile.data.repository

import com.sinc.mobile.data.local.dao.UnidadProductivaDao
import com.sinc.mobile.data.local.entities.UnidadProductivaEntity
import com.sinc.mobile.data.network.api.UnidadProductivaApiService
import com.sinc.mobile.data.network.dto.request.CreateUnidadProductivaRequest
import com.sinc.mobile.data.network.dto.request.UpdateUnidadProductivaRequest
import com.sinc.mobile.data.network.dto.response.UnidadProductivaDto
import com.sinc.mobile.data.session.SessionManager
import com.sinc.mobile.domain.model.CreateUnidadProductivaData
import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.model.UpdateUnidadProductivaData
import com.sinc.mobile.domain.repository.UnidadProductivaRepository
import com.sinc.mobile.domain.util.Error
import com.sinc.mobile.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

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

    override suspend fun syncUnidadesProductivas(): Result<Unit, Error> {
        return try {
            val response = unidadProductivaApiService.getUnidadesProductivas()
            if (response.isSuccessful) {
                val dtos = response.body()
                if (dtos != null) {
                    unidadProductivaDao.clearAndInsert(dtos.map { it.toEntity() })
                    Result.Success(Unit)
                } else {
                    Result.Failure(GenericError("El cuerpo de la respuesta de unidades productivas es nulo"))
                }
            } else {
                Result.Failure(GenericError("Error de API al sincronizar unidades productivas: ${response.code()} - ${response.message()}"))
            }
        } catch (e: IOException) {
            Result.Failure(GenericError("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.Failure(GenericError("Error inesperado: ${e.message}"))
        }
    }

    override suspend fun createUnidadProductiva(data: CreateUnidadProductivaData): Result<UnidadProductiva, Error> {
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
                    unidadProductivaDao.insertUnidadProductiva(dto.toEntity())
                    Result.Success(dto.toDomain())
                } else {
                    Result.Failure(GenericError("El cuerpo de la respuesta de creación de unidad productiva es nulo"))
                }
            } else {
                Result.Failure(GenericError("Error de API al crear unidad productiva: ${response.code()} - ${response.message()}"))
            }
        } catch (e: IOException) {
            Result.Failure(GenericError("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.Failure(GenericError("Error inesperado: ${e.message}"))
        }
    }

    override suspend fun updateUnidadProductiva(id: Int, data: UpdateUnidadProductivaData): Result<UnidadProductiva, Error> {
        val request = UpdateUnidadProductivaRequest(
            superficie = data.superficie,
            condicionTenenciaId = data.condicionTenenciaId,
            aguaAnimalFuenteId = data.aguaAnimalFuenteId,
            aguaHumanoFuenteId = data.aguaHumanoFuenteId,
            aguaHumanoEnCasa = data.aguaHumanoEnCasa?.let { if (it) 1 else 0 },
            aguaHumanoDistancia = data.aguaHumanoDistancia,
            aguaAnimalDistancia = data.aguaAnimalDistancia,
            tipoSueloId = data.tipoSueloId,
            tipoPastoId = data.tipoPastoId,
            forrajerasPredominante = data.forrajerasPredominante?.let { if (it) 1 else 0 },
            habita = data.habita?.let { if (it) 1 else 0 },
            observaciones = data.observaciones
        )

        return try {
            val response = unidadProductivaApiService.updateUnidadProductiva(id, request)
            if (response.isSuccessful) {
                val dto = response.body()
                if (dto != null) {
                    unidadProductivaDao.insertUnidadProductiva(dto.toEntity())
                    Result.Success(dto.toDomain())
                } else {
                    Result.Failure(GenericError("El cuerpo de la respuesta de actualización de unidad productiva es nulo"))
                }
            } else {
                Result.Failure(GenericError("Error de API al actualizar unidad productiva: ${response.code()} - ${response.message()}"))
            }
        } catch (e: IOException) {
            Result.Failure(GenericError("Error de red: ${e.message}"))
        } catch (e: Exception) {
            Result.Failure(GenericError("Error inesperado: ${e.message}"))
        }
    }
}


private fun UnidadProductivaDto.toEntity(): UnidadProductivaEntity {
    return UnidadProductivaEntity(
        id = this.id,
        nombre = this.nombre ?: "",
        identificadorLocal = this.identificadorLocal,
        superficie = this.superficie?.toFloatOrNull() ?: 0f,
        latitud = this.latitud?.toDoubleOrNull(),
        longitud = this.longitud?.toDoubleOrNull(),
        municipioId = this.municipioId ?: 0,
        condicionTenenciaId = this.pivot?.condicionTenenciaId,
        aguaHumanoFuenteId = this.fuenteAguaId,
        aguaHumanoEnCasa = this.aguaHumanoEnCasa == 1,
        aguaHumanoDistancia = this.aguaHumanoDistancia,
        aguaAnimalFuenteId = this.aguaAnimalFuenteId,
        aguaAnimalDistancia = this.aguaAnimalDistancia,
        tipoSueloId = this.tipoSueloId,
        tipoPastoId = this.tipoPastoId,
        forrajerasPredominante = this.forrajerasPredominante == 1,
        habita = this.habita == 1,
        observaciones = this.observaciones,
        activo = this.activo == 1,
        completo = this.completo == 1
    )
}

private fun UnidadProductivaDto.toDomain(): UnidadProductiva {
    return UnidadProductiva(
        id = this.id,
        nombre = this.nombre,
        identificadorLocal = this.identificadorLocal,
        superficie = this.superficie?.toFloatOrNull(),
        latitud = this.latitud?.toDoubleOrNull(),
        longitud = this.longitud?.toDoubleOrNull(),
        municipioId = this.municipioId,
        condicionTenenciaId = this.pivot?.condicionTenenciaId,
        aguaHumanoFuenteId = this.fuenteAguaId,
        aguaHumanoEnCasa = this.aguaHumanoEnCasa == 1,
        aguaHumanoDistancia = this.aguaHumanoDistancia,
        aguaAnimalFuenteId = this.aguaAnimalFuenteId,
        aguaAnimalDistancia = this.aguaAnimalDistancia,
        tipoSueloId = this.tipoSueloId,
        tipoPastoId = this.tipoPastoId,
        forrajerasPredominante = this.forrajerasPredominante == 1,
        habita = this.habita == 1,
        observaciones = this.observaciones
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
        aguaHumanoFuenteId = this.aguaHumanoFuenteId,
        aguaHumanoEnCasa = this.aguaHumanoEnCasa,
        aguaHumanoDistancia = this.aguaHumanoDistancia,
        aguaAnimalFuenteId = this.aguaAnimalFuenteId,
        aguaAnimalDistancia = this.aguaAnimalDistancia,
        tipoSueloId = this.tipoSueloId,
        tipoPastoId = this.tipoPastoId,
        forrajerasPredominante = this.forrajerasPredominante,
        habita = this.habita,
        observaciones = this.observaciones
    )
}