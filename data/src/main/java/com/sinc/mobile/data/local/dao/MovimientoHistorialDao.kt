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
    suspend fun insertAll(movimientos: List<MovimientoHistorialEntity>)

    @Query("DELETE FROM movimiento_historial")
    suspend fun clearAll()

    @Transaction
    suspend fun clearAndInsert(movimientos: List<MovimientoHistorialEntity>) {
        clearAll()
        insertAll(movimientos)
    }
}
