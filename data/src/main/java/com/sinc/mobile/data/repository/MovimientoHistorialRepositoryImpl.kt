package com.sinc.mobile.data.repository

import android.content.SharedPreferences
import com.sinc.mobile.data.local.dao.MovimientoHistorialDao
import com.sinc.mobile.data.mapper.toDomain
import com.sinc.mobile.data.mapper.toEntity
import com.sinc.mobile.data.network.api.HistorialMovimientosApiService
import com.sinc.mobile.data.network.api.MovimientoApiService
import com.sinc.mobile.data.network.dto.MovimientoRequest
import com.sinc.mobile.data.network.dto.MovimientosBatchRequest
import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.model.MovimientoHistorial
import com.sinc.mobile.domain.repository.MovimientoHistorialRepository
import com.sinc.mobile.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MovimientoHistorialRepositoryImpl @Inject constructor(
    private val apiService: HistorialMovimientosApiService,
    private val syncApiService: MovimientoApiService,
    private val dao: MovimientoHistorialDao,
    private val prefs: SharedPreferences
) : MovimientoHistorialRepository {

    override fun getMovimientos(): Flow<List<MovimientoHistorial>> {
        return dao.getAllMovimientos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncMovimientos(lastSyncTimestamp: String?): Result<Unit, GenericError> {
        return try {
            val localCount = dao.getMovimientoCount()
            val effectiveTimestamp = if (localCount == 0) {
                null
            } else {
                lastSyncTimestamp
            }

            val response = apiService.getHistorialMovimientos(updatedAfter = effectiveTimestamp)
            if (response.isSuccessful) {
                val dtos = response.body()
                if (dtos != null) {
                    if (dtos.isNotEmpty()) {
                        try {
                            val entities = dtos.map { it.toEntity() }
                            
                            if (effectiveTimestamp == null) {
                                dao.clearAndInsert(entities)
                            } else {
                                // Important: delete local records that were just synced but don't have a server ID yet
                                // to avoid duplicates before inserting the official server version
                                dao.deleteTemporarySyncedMovements()
                                dao.insertAll(entities)
                            }

                            val latestMovimiento = dtos.maxByOrNull { it.fechaRegistro }
                            latestMovimiento?.let {
                                val newWaterMark = it.fechaRegistro
                                
                                if (effectiveTimestamp == null || newWaterMark > (effectiveTimestamp ?: "")) {
                                    saveLastSyncTimestamp(newWaterMark)
                                }
                            }
                        } catch (e: Exception) {
                            return Result.Failure(GenericError("DB Error: ${e.message}"))
                        }
                    } 
                    Result.Success(Unit)
                } else {
                    Result.Failure(GenericError("Response body is null"))
                }
            } else {
                Result.Failure(GenericError("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Failure(GenericError("Network Error: ${e.message ?: "Unknown"}"))
        }
    }

    override suspend fun saveLocalMovimiento(movimiento: MovimientoHistorial): Result<Long, GenericError> {
        return try {
            val entity = movimiento.toEntity().copy(sincronizado = false)
            val localId = dao.insert(entity)
            Result.Success(localId)
        } catch (e: Exception) {
            Result.Failure(GenericError("Error al guardar localmente: ${e.message}"))
        }
    }

    override suspend fun syncUnsyncedMovements(): Result<Unit, GenericError> {
        return try {
            val unsynced = dao.getUnsyncedMovements()
            if (unsynced.isEmpty()) return Result.Success(Unit)

            // La API agrupa los movimientos por UP en la request, así que los agrupamos localmente
            val groupedByUp = unsynced.groupBy { it.unidadProductivaId }
            
            var allSuccess = true
            var lastError = ""

            for ((upId, movimientos) in groupedByUp) {
                val batch = MovimientosBatchRequest(
                    upId = upId,
                    movimientos = movimientos.map {
                        MovimientoRequest(
                            especie_id = it.especieId,
                            categoria_id = it.categoriaId,
                            raza_id = it.razaId,
                            cantidad = it.cantidad,
                            motivo_movimiento_id = it.motivoId,
                            destino_traslado = it.destinoTraslado
                        )
                    }
                )

                val response = syncApiService.saveMovimientos(batch)
                if (response.isSuccessful) {
                    // Marcar este lote como sincronizado (dejamos id en null para que sea limpiado en la delta sync)
                    movimientos.forEach {
                        dao.markAsSynced(it.localId, null)
                    }
                } else {
                    allSuccess = false
                    lastError = "Error en UP $upId: ${response.code()}"
                }
            }

            if (allSuccess) {
                Result.Success(Unit)
            } else {
                Result.Failure(GenericError(lastError))
            }
        } catch (e: Exception) {
            Result.Failure(GenericError("Error de red: ${e.message}"))
        }
    }

    override suspend fun getLastSyncTimestamp(): String? {
        return prefs.getString("last_sync_movimientos", null)
    }

    override suspend fun saveLastSyncTimestamp(timestamp: String) {
        prefs.edit().putString("last_sync_movimientos", timestamp).apply()
    }
}
