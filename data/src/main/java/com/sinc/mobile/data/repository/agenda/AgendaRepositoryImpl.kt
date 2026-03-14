package com.sinc.mobile.data.repository.agenda

import com.sinc.mobile.data.local.dao.AgendaDao
import com.sinc.mobile.data.mapper.agenda.toDomain
import com.sinc.mobile.data.mapper.agenda.toDto
import com.sinc.mobile.data.mapper.agenda.toEntity
import com.sinc.mobile.data.network.api.agenda.AgendaApiService
import com.sinc.mobile.domain.model.agenda.AgendaItem
import com.sinc.mobile.domain.repository.agenda.AgendaRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.util.TimeManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgendaRepositoryImpl @Inject constructor(
    private val agendaDao: AgendaDao,
    private val apiService: AgendaApiService,
    private val timeManager: TimeManager
) : AgendaRepository {

    override fun getAgendaItems(): Flow<List<AgendaItem>> {
        return agendaDao.getAllAgendaItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAgendaItemById(id: Long): Flow<AgendaItem?> {
        return agendaDao.getAgendaItemById(id).map { it?.toDomain() }
    }

    override suspend fun syncAgendaItems(): Result<Unit, Error> {
        return try {
            val response = apiService.getAgendaItems()
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.map { it.toEntity(timeManager) }
                agendaDao.clearAndInsert(items)
                Result.Success(Unit)
            } else {
                Result.Failure(GenericError("Error al sincronizar agenda: ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.Failure(GenericError("Error de red al sincronizar agenda"))
        } catch (e: Exception) {
            Result.Failure(GenericError(e.message ?: "Error desconocido al sincronizar agenda"))
        }
    }

    override suspend fun saveAgendaItem(item: AgendaItem): Result<AgendaItem, Error> {
        return try {
            val response = apiService.saveAgendaItem(item.toDto(timeManager))
            if (response.isSuccessful && response.body() != null) {
                val savedDto = response.body()!!
                val entity = savedDto.toEntity(timeManager)
                agendaDao.insertAgendaItem(entity)
                Result.Success(entity.toDomain())
            } else {
                Result.Failure(GenericError("Error al guardar ítem de agenda: ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.Failure(GenericError("Error de red al guardar ítem de agenda"))
        } catch (e: Exception) {
            Result.Failure(GenericError(e.message ?: "Error desconocido al guardar ítem de agenda"))
        }
    }

    override suspend fun deleteAgendaItem(id: Long): Result<Unit, Error> {
        return try {
            val response = apiService.deleteAgendaItem(id)
            if (response.isSuccessful) {
                agendaDao.deleteAgendaItemById(id)
                Result.Success(Unit)
            } else {
                Result.Failure(GenericError("Error al eliminar ítem: ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.Failure(GenericError("Error de red al eliminar ítem"))
        } catch (e: Exception) {
            Result.Failure(GenericError(e.message ?: "Error desconocido al eliminar ítem"))
        }
    }

    override suspend fun toggleAgendaStatus(id: Long, isCompleted: Boolean): Result<Unit, Error> {
        return try {
            val response = apiService.updateAgendaStatus(id, isCompleted)
            if (response.isSuccessful) {
                syncAgendaItems()
                Result.Success(Unit)
            } else {
                Result.Failure(GenericError("Error al actualizar estado: ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.Failure(GenericError("Error de red al actualizar estado"))
        } catch (e: Exception) {
            Result.Failure(GenericError(e.message ?: "Error desconocido al actualizar estado"))
        }
    }
}
