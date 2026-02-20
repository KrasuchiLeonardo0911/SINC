package com.sinc.mobile.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.sinc.mobile.data.local.dao.MovimientoHistorialDao
import com.sinc.mobile.data.mapper.toDomain
import com.sinc.mobile.data.mapper.toEntity
import com.sinc.mobile.data.network.api.HistorialMovimientosApiService
import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.model.MovimientoHistorial
import com.sinc.mobile.domain.repository.MovimientoHistorialRepository
import com.sinc.mobile.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class MovimientoHistorialRepositoryImpl @Inject constructor(
    private val apiService: HistorialMovimientosApiService,
    private val dao: MovimientoHistorialDao,
    private val prefs: SharedPreferences
) : MovimientoHistorialRepository {

    override fun getMovimientos(): Flow<List<MovimientoHistorial>> {
        return dao.getAllMovimientos().map { entities ->
            Log.d("SyncDebug", "DAO emitted ${entities.size} movements from DB")
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

            Log.d("SyncDebug", "Starting sync. Local count: $localCount. Effective timestamp: $effectiveTimestamp")

            val response = apiService.getHistorialMovimientos(updatedAfter = effectiveTimestamp)
            if (response.isSuccessful) {
                val dtos = response.body()
                if (dtos != null) {
                    Log.d("SyncDebug", "API Success. Received ${dtos.size} items.")
                    
                    if (dtos.isNotEmpty()) {
                        try {
                            // 1. Process Data
                            val entities = dtos.map { it.toEntity() }
                            Log.d("SyncDebug", "Mapped ${entities.size} entities. First ID: ${entities.firstOrNull()?.id}, Last ID: ${entities.lastOrNull()?.id}")
                            
                            if (effectiveTimestamp == null) {
                                Log.d("SyncDebug", "Performing Full Sync (Clear & Insert)")
                                dao.clearAndInsert(entities)
                            } else {
                                Log.d("SyncDebug", "Performing Delta Sync (Insert/Update)")
                                dao.insertAll(entities)
                            }
                            Log.d("SyncDebug", "DB Transaction Complete. New Local Count: ${dao.getMovimientoCount()}")

                            // 2. Update Timestamp using the latest record found
                            // IMPORTANT: We find the max date from the received items.
                            // To be safer against clock drift, we could subtract a few seconds, 
                            // but usually the server 'updated_after' is inclusive or exclusive depending on backend.
                            // Laravel's 'where('updated_at', '>', $timestamp)' is usually exclusive.
                            
                            val latestMovimiento = dtos.maxByOrNull { it.fechaRegistro }
                            latestMovimiento?.let {
                                val newWaterMark = it.fechaRegistro
                                
                                // Only update if the new watermark is actually newer than what we had
                                // or if we didn't have one.
                                if (effectiveTimestamp == null || newWaterMark > (effectiveTimestamp ?: "")) {
                                    Log.d("SyncDebug", "Saving new High Water Mark: $newWaterMark")
                                    saveLastSyncTimestamp(newWaterMark)
                                } else {
                                    Log.d("SyncDebug", "Received data but none newer than current watermark ($effectiveTimestamp). Not updating watermark.")
                                }
                            }

                        } catch (e: Exception) {
                            Log.e("SyncDebug", "Error inserting/mapping data: ${e.message}", e)
                            return Result.Failure(GenericError("DB Error: ${e.message}"))
                        }
                    } else {
                        // Empty list
                        if (effectiveTimestamp == null) {
                            Log.d("SyncDebug", "API returned empty list on full sync. Clearing DB.")
                            dao.clearAll()
                            // If full sync returns nothing, we don't have a watermark. 
                            // We leave it null/unchanged so next time we try full sync again? 
                            // Or we set it to "now"? Setting to "now" is risky if we don't know server TZ.
                            // Safest: Leave it, or set to a very old date? 
                            // Better: Don't save anything. Next time effectiveTimestamp is null, checks again.
                            // If user creates data, we get it.
                        } else {
                            Log.d("SyncDebug", "No new items found (Delta Sync). Keeping timestamp: $effectiveTimestamp")
                        }
                    }

                    Result.Success(Unit)
                } else {
                    Log.e("SyncDebug", "Response body is null")
                    Result.Failure(GenericError("Response body is null"))
                }
            } else {
                Log.e("SyncDebug", "API Error: ${response.code()} ${response.message()}")
                val errorBody = response.errorBody()?.string()
                Log.e("SyncDebug", "Error Body: $errorBody")
                Result.Failure(GenericError("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("SyncDebug", "Network/Unexpected Error: ${e.message}", e)
            Result.Failure(GenericError("Network Error: ${e.message ?: "Unknown"}"))
        }
    }

    override suspend fun getLastSyncTimestamp(): String? {
        val ts = prefs.getString("last_sync_movimientos", null)
        Log.d("SyncDebug", "Retrieved last sync timestamp: $ts")
        return ts
    }

    override suspend fun saveLastSyncTimestamp(timestamp: String) {
        prefs.edit().putString("last_sync_movimientos", timestamp).apply()
    }
}