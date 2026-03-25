package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sinc.mobile.data.local.entities.MovimientoHistorialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoHistorialDao {

    @Query("SELECT * FROM movimiento_historial ORDER BY fechaRegistro DESC")
    fun getAllMovimientos(): Flow<List<MovimientoHistorialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movimiento: MovimientoHistorialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movimientos: List<MovimientoHistorialEntity>)

    @Query("DELETE FROM movimiento_historial")
    suspend fun clearAll()

    @Transaction
    suspend fun clearAndInsert(movimientos: List<MovimientoHistorialEntity>) {
        // Only clear the ones that ARE synced to not lose local data
        clearSyncedMovements()
        insertAll(movimientos)
    }

    @Query("DELETE FROM movimiento_historial WHERE sincronizado = 1")
    suspend fun clearSyncedMovements()

    @Query("SELECT COUNT(*) FROM movimiento_historial")
    suspend fun getMovimientoCount(): Int

    @Query("SELECT * FROM movimiento_historial WHERE sincronizado = 0")
    suspend fun getUnsyncedMovements(): List<MovimientoHistorialEntity>

    @Query("UPDATE movimiento_historial SET id = :serverId, sincronizado = 1 WHERE localId = :localId")
    suspend fun markAsSynced(localId: Long, serverId: Long)
}
