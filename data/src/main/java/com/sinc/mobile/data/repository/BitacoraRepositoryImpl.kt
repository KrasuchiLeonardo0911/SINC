package com.sinc.mobile.data.repository

import com.sinc.mobile.data.local.dao.BitacoraDao
import com.sinc.mobile.data.local.entities.BitacoraEntity
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class BitacoraRepositoryImpl @Inject constructor(
    private val api: BitacoraApiService,
    private val dao: BitacoraDao
) : BitacoraRepository {

    // Scope para tareas de fondo que no deben bloquear la UI
    private val externalScope = CoroutineScope(Dispatchers.IO)

    override fun getBitacoras(): Flow<List<Bitacora>> {
        return dao.getAllBitacoras().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncBitacoras(): Result<Unit, Error> {
        return try {
            // 1. Antes de bajar, intentamos subir lo pendiente
            uploadPendingBitacoras()

            // 2. Bajamos lo último del servidor
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
            // 1. GUARDADO LOCAL INMEDIATO (Optimista)
            val entity = BitacoraEntity(
                userId = 0, // El servidor lo asignarÃ¡ o lo sacamos de la sesiÃ³n
                fecha = fecha.atStartOfDay(),
                contenido = contenido,
                sincronizado = false
            )
            val localId = dao.insert(entity).toInt()
            val savedLocal = entity.copy(localId = localId)

            // 2. INTENTO DE RED EN SEGUNDO PLANO (No bloquea la UI)
            externalScope.launch {
                performSaveApi(savedLocal)
            }

            // Devolvemos el objeto local al ViewModel inmediatamente
            Result.Success(savedLocal.toDomain())
        } catch (e: Exception) {
            Result.Failure(GenericError(e.message ?: "Error local"))
        }
    }

    private suspend fun performSaveApi(localEntity: BitacoraEntity) {
        try {
            val request = CreateBitacoraRequest(
                fecha = localEntity.fecha.format(DateTimeFormatter.ISO_LOCAL_DATE),
                contenido = localEntity.contenido
            )
            val response = api.createBitacora(request)
            if (response.isSuccessful && response.body() != null) {
                val serverDto = response.body()!!
                // Marcamos como sincronizado con el ID real del servidor
                dao.markAsSynced(localEntity.localId, serverDto.id)
            }
        } catch (e: Exception) {
            // Si falla, se queda en la DB como sincronizado = 0 para el prÃ³ximo refresh
        }
    }

    override suspend fun updateBitacora(id: Int, contenido: String): Result<Bitacora, Error> {
        return try {
            // id aquÃ­ es el localId gracias al mapper
            val localEntity = dao.getBitacoraByLocalId(id)
            if (localEntity != null) {
                val updatedLocal = localEntity.copy(contenido = contenido, sincronizado = false)
                dao.insert(updatedLocal)

                externalScope.launch {
                    performUpdateApi(updatedLocal)
                }
                Result.Success(updatedLocal.toDomain())
            } else {
                Result.Failure(GenericError("Registro no encontrado localmente"))
            }
        } catch (e: Exception) {
            Result.Failure(GenericError(e.message ?: "Error local"))
        }
    }

    private suspend fun performUpdateApi(localEntity: BitacoraEntity) {
        // Solo si ya tiene un ID de servidor podemos actualizar
        val serverId = localEntity.id ?: return 
        try {
            val request = UpdateBitacoraRequest(contenido = localEntity.contenido)
            val response = api.updateBitacora(serverId, request)
            if (response.isSuccessful) {
                dao.markAsSynced(localEntity.localId, serverId)
            }
        } catch (e: Exception) { }
    }

    override suspend fun deleteBitacora(id: Int): Result<Unit, Error> {
        return try {
            val localEntity = dao.getBitacoraByLocalId(id)
            val serverId = localEntity?.id
            
            // Borramos localmente siempre
            dao.deleteByLocalId(id)

            if (serverId != null) {
                externalScope.launch {
                    try { api.deleteBitacora(serverId) } catch (e: Exception) {}
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(GenericError(e.message ?: "Error local"))
        }
    }

    private suspend fun uploadPendingBitacoras() {
        val pending = dao.getUnsyncedBitacoras()
        pending.forEach { 
            if (it.id == null) performSaveApi(it) else performUpdateApi(it)
        }
    }
}
