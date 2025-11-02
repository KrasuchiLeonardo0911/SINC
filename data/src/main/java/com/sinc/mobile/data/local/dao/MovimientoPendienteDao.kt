package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sinc.mobile.data.local.entities.MovimientoPendienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoPendienteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movimiento: MovimientoPendienteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movimientos: List<MovimientoPendienteEntity>)

    @Query("SELECT * FROM movimientos_pendientes WHERE sincronizado = 0")
    fun getUnsyncedMovimientos(): Flow<List<MovimientoPendienteEntity>>

    @Query("SELECT * FROM movimientos_pendientes")
    fun getAllMovimientosPendientes(): Flow<List<MovimientoPendienteEntity>>

    @Query("UPDATE movimientos_pendientes SET sincronizado = 1 WHERE id = :movimientoId")
    suspend fun markAsSynced(movimientoId: Long)

    @Query("DELETE FROM movimientos_pendientes")
    suspend fun clearAll()
}
