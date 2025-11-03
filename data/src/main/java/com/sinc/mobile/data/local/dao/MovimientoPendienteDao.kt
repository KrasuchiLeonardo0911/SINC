package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinc.mobile.data.local.entities.MovimientoPendienteEntity
import kotlinx.coroutines.flow.Flow
import androidx.room.Delete
import androidx.room.Update

@Dao
interface MovimientoPendienteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movimiento: MovimientoPendienteEntity)

    @Query("SELECT * FROM movimientos_pendientes")
    fun getAll(): Flow<List<MovimientoPendienteEntity>>

    @Delete
    suspend fun delete(movimiento: MovimientoPendienteEntity)

    @Update
    suspend fun update(movimiento: MovimientoPendienteEntity)
}
