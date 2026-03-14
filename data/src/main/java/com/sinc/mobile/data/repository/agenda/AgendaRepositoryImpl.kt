package com.sinc.mobile.data.repository.agenda

import com.sinc.mobile.data.local.dao.AgendaDao
import com.sinc.mobile.data.local.entities.agenda.AgendaEntity
import com.sinc.mobile.data.mapper.agenda.toDomain
import com.sinc.mobile.data.mapper.agenda.toDto
import com.sinc.mobile.data.mapper.agenda.toEntity
import com.sinc.mobile.data.network.api.agenda.AgendaApiService
import com.sinc.mobile.data.network.dto.agenda.UpdateAgendaStatusRequest
import com.sinc.mobile.domain.model.agenda.AgendaItem
import com.sinc.mobile.domain.repository.agenda.AgendaRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.util.TimeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgendaRepositoryImpl @Inject constructor(
    private val agendaDao: AgendaDao,
    private val apiService: AgendaApiService,
    private val timeManager: TimeManager
) : AgendaRepository {

    private val externalScope = CoroutineScope(Dispatchers.IO)

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
            uploadPendingAgendaItems()
            val response = apiService.getAgendaItems()
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.map { it.toEntity(timeManager) }
                agendaDao.clearAndInsert(items)
                Result.Success(Unit)
            } else {
                Result.Failure(GenericError("Error al sincronizar agenda: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Failure(GenericError(e.message ?: "Error de red"))
        }
    }

    override suspend fun saveAgendaItem(item: AgendaItem): Result<AgendaItem, Error> {
        return try {
            val entity = item.toEntity().copy(sincronizado = false)
            val localId = agendaDao.insertAgendaItem(entity)
            val savedLocal = entity.copy(localId = localId)

            externalScope.launch {
                performSaveApi(savedLocal)
            }

            Result.Success(savedLocal.toDomain())
        } catch (e: Exception) {
            Result.Failure(GenericError("Error local al guardar"))
        }
    }

    private suspend fun performSaveApi(localEntity: AgendaEntity) {
        try {
            val response = apiService.saveAgendaItem(localEntity.toDomain().toDto(timeManager))
            if (response.isSuccessful && response.body() != null) {
                val serverDto = response.body()!!
                agendaDao.markAsSynced(localEntity.localId, serverDto.id)
            }
        } catch (e: Exception) { }
    }

    override suspend fun deleteAgendaItem(id: Long): Result<Unit, Error> {
        return try {
            val entity = agendaDao.getAgendaItemByLocalId(id)
            val serverId = entity?.id
            
            agendaDao.deleteAgendaItemByLocalId(id)

            if (serverId != null) {
                externalScope.launch {
                    try { apiService.deleteAgendaItem(serverId) } catch (e: Exception) {}
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(GenericError("Error local al eliminar"))
        }
    }

    override suspend fun toggleAgendaStatus(id: Long, isCompleted: Boolean): Result<Unit, Error> {        
        return try {
            val completedAt = if (isCompleted) java.time.LocalDateTime.now() else null
            agendaDao.updateStatus(id, completedAt)

            externalScope.launch {
                val entity = agendaDao.getAgendaItemByLocalId(id)
                val serverId = entity?.id
                if (serverId != null) {
                    apiService.updateAgendaStatus(serverId, UpdateAgendaStatusRequest(completada = isCompleted))
                    // Al ser una actualizaciÃ³n de estado, no necesitamos marcarAsSynced de nuevo 
                    // si el servidor no devuelve un nuevo objeto, pero podemos hacerlo para asegurar.
                    agendaDao.markAsSynced(id, serverId)
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(GenericError("Error al actualizar estado"))
        }
    }

    private suspend fun uploadPendingAgendaItems() {
        val pending = agendaDao.getUnsyncedAgendaItems()
        pending.forEach { 
            if (it.id == null) performSaveApi(it)
        }
    }
}
