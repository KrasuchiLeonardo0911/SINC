package com.sinc.mobile.data.repository

import com.sinc.mobile.data.local.dao.UnidadProductivaDao
import com.sinc.mobile.data.local.entities.UnidadProductivaEntity
import com.sinc.mobile.data.local.entities.UnidadProductivaTipoPastoCrossRef
import com.sinc.mobile.data.local.entities.UnidadProductivaTipoSueloCrossRef
import com.sinc.mobile.data.network.api.UnidadProductivaApiService
import com.sinc.mobile.data.network.dto.request.CreateUnidadProductivaRequest
import com.sinc.mobile.data.network.dto.request.PastoRequestDto
import com.sinc.mobile.data.network.dto.request.SueloRequestDto
import com.sinc.mobile.data.network.dto.request.UpdateUnidadProductivaRequest
import com.sinc.mobile.data.network.dto.response.UnidadProductivaDto
import com.sinc.mobile.data.network.dto.response.TipoSueloPivotDto
import com.sinc.mobile.data.network.dto.response.RecursoForrajeroPivotDto
import com.sinc.mobile.domain.model.*
import com.sinc.mobile.domain.repository.UnidadProductivaRepository
import com.sinc.mobile.domain.util.Error
import com.sinc.mobile.domain.util.Result
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.combine

class UnidadProductivaRepositoryImpl @Inject constructor(
    private val unidadProductivaApiService: UnidadProductivaApiService,
    private val unidadProductivaDao: UnidadProductivaDao
) : UnidadProductivaRepository {

    override fun getUnidadesProductivas(): Flow<List<UnidadProductiva>> {
        return combine(
            unidadProductivaDao.getAllUnidadesProductivas(),
            unidadProductivaDao.getAllSuelosCrossRef(),
            unidadProductivaDao.getAllPastosCrossRef(),
            unidadProductivaDao.getAllTiposSuelo(),
            unidadProductivaDao.getAllTiposPasto()
        ) { unidades, suelosCrossRef, pastosCrossRef, tiposSuelo, tiposPasto ->
            val tiposSueloMap = tiposSuelo.associateBy { it.id }
            val tiposPastoMap = tiposPasto.associateBy { it.id }

            val suelosAgrupados = suelosCrossRef.groupBy { it.unidadProductivaId }
            val pastosAgrupados = pastosCrossRef.groupBy { it.unidadProductivaId }

            unidades.map { unidad ->
                val suelosInfo = suelosAgrupados[unidad.id].orEmpty().mapNotNull { crossRef ->
                    tiposSueloMap[crossRef.tipoSueloId]?.let { tipoSuelo ->
                        SueloInfo(
                            id = tipoSuelo.id,
                            nombre = tipoSuelo.nombre,
                            porcentaje = crossRef.porcentaje
                        )
                    }
                }

                val pastosInfo = pastosAgrupados[unidad.id].orEmpty().mapNotNull { crossRef ->
                    tiposPastoMap[crossRef.tipoPastoId]?.let { tipoPasto ->
                        PastoInfo(
                            id = tipoPasto.id,
                            nombre = tipoPasto.nombre,
                            porcentaje = crossRef.porcentaje
                        )
                    }
                }
                
                unidad.toDomain(suelosInfo, pastosInfo)
            }
        }
    }

    override fun getUnidadProductivaById(id: Int): Flow<UnidadProductiva?> {
        return combine(
            unidadProductivaDao.getUnidadProductivaById(id),
            unidadProductivaDao.getAllSuelosCrossRef(),
            unidadProductivaDao.getAllPastosCrossRef(),
            unidadProductivaDao.getAllTiposSuelo(),
            unidadProductivaDao.getAllTiposPasto()
        ) { unidad, suelosCrossRef, pastosCrossRef, tiposSuelo, tiposPasto ->
            val currentUnidad = unidad ?: return@combine null

            val tiposSueloMap = tiposSuelo.associateBy { it.id }
            val tiposPastoMap = tiposPasto.associateBy { it.id }

            val suelosAgrupados = suelosCrossRef.groupBy { it.unidadProductivaId }
            val pastosAgrupados = pastosCrossRef.groupBy { it.unidadProductivaId }

            val suelosInfo = suelosAgrupados[currentUnidad.id].orEmpty().mapNotNull { crossRef ->
                tiposSueloMap[crossRef.tipoSueloId]?.let { tipoSuelo ->
                    SueloInfo(
                        id = tipoSuelo.id,
                        nombre = tipoSuelo.nombre,
                        porcentaje = crossRef.porcentaje
                    )
                }
            }

            val pastosInfo = pastosAgrupados[currentUnidad.id].orEmpty().mapNotNull { crossRef ->
                tiposPastoMap[crossRef.tipoPastoId]?.let { tipoPasto ->
                    PastoInfo(
                        id = tipoPasto.id,
                        nombre = tipoPasto.nombre,
                        porcentaje = crossRef.porcentaje
                    )
                }
            }

            currentUnidad.toDomain(suelosInfo, pastosInfo)
        }
    }

    override suspend fun syncUnidadesProductivas(): Result<Unit, Error> {
        return try {
            val response = unidadProductivaApiService.getUnidadesProductivas()
            if (response.isSuccessful) {
                val dtos = response.body()
                if (dtos != null) {
                    val unidades = dtos.map { it.toEntity() }

                    val suelos = mutableListOf<UnidadProductivaTipoSueloCrossRef>()
                    dtos.forEach { dto ->
                        dto.tiposSuelo.forEach { sueloDto ->
                            suelos.add(
                                UnidadProductivaTipoSueloCrossRef(
                                    unidadProductivaId = dto.id,
                                    tipoSueloId = sueloDto.id,
                                    porcentaje = sueloDto.pivot.porcentaje
                                )
                            )
                        }
                    }

                    val pastos = mutableListOf<UnidadProductivaTipoPastoCrossRef>()
                    dtos.forEach { dto ->
                        dto.recursosForrajeros.forEach { pastoDto ->
                            pastos.add(
                                UnidadProductivaTipoPastoCrossRef(
                                    unidadProductivaId = dto.id,
                                    tipoPastoId = pastoDto.id,
                                    porcentaje = pastoDto.pivot.porcentaje ?: 0
                                )
                            )
                        }
                    }
                    
                    unidadProductivaDao.clearAndInsert(unidades, suelos, pastos)
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
            parajeId = data.parajeId,
            fechaInicio = data.fechaInicio,
            tiposSuelo = data.tiposSuelo?.map { SueloRequestDto(it.id, it.porcentaje) },
            recursosForrajeros = data.recursosForrajeros?.map { PastoRequestDto(it.id, it.porcentaje) }
        )

        return try {
            val response = unidadProductivaApiService.createUnidadProductiva(request)
            if (response.isSuccessful) {
                val dto = response.body()
                if (dto != null) {
                    val syncResult = syncUnidadesProductivas()
                    if (syncResult is Result.Failure) {
                        return Result.Failure(syncResult.error)
                    }
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
            habita = data.habita?.let { if (it) 1 else 0 },
            observaciones = data.observaciones,
            tiposSuelo = data.tiposSuelo?.map { SueloRequestDto(it.id, it.porcentaje) },
            recursosForrajeros = data.recursosForrajeros?.map { PastoRequestDto(it.id, it.porcentaje) }
        )

        return try {
            val response = unidadProductivaApiService.updateUnidadProductiva(id, request)
            if (response.isSuccessful) {
                val dto = response.body()
                if (dto != null) {
                    val syncResult = syncUnidadesProductivas()
                    if (syncResult is Result.Failure) {
                        return Result.Failure(syncResult.error)
                    }
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
        condicionTenenciaId = this.condicionTenenciaId ?: this.pivot?.condicionTenenciaId,
        aguaHumanoFuenteId = this.fuenteAguaId,
        aguaHumanoEnCasa = this.aguaHumanoEnCasa == 1,
        aguaHumanoDistancia = this.aguaHumanoDistancia,
        aguaAnimalFuenteId = this.aguaAnimalFuenteId,
        aguaAnimalDistancia = this.aguaAnimalDistancia,
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
        condicionTenenciaId = this.condicionTenenciaId ?: this.pivot?.condicionTenenciaId,
        aguaHumanoFuenteId = this.fuenteAguaId,
        aguaHumanoEnCasa = this.aguaHumanoEnCasa == 1,
        aguaHumanoDistancia = this.aguaHumanoDistancia,
        aguaAnimalFuenteId = this.aguaAnimalFuenteId,
        aguaAnimalDistancia = this.aguaAnimalDistancia,
        habita = this.habita == 1,
        observaciones = this.observaciones,
        tiposSuelo = this.tiposSuelo?.map { SueloInfo(it.id, it.nombre, it.pivot.porcentaje) } ?: emptyList(),
        recursosForrajeros = this.recursosForrajeros?.map { PastoInfo(it.id, it.nombre, it.pivot.porcentaje) } ?: emptyList()
    )
}

private fun UnidadProductivaEntity.toDomain(suelos: List<SueloInfo>, pastos: List<PastoInfo>): UnidadProductiva {
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
        habita = this.habita,
        observaciones = this.observaciones,
        tiposSuelo = suelos,
        recursosForrajeros = pastos
    )
}
