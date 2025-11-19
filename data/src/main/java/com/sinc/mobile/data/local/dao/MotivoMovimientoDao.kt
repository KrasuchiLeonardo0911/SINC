package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinc.mobile.data.local.entities.MotivoMovimientoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MotivoMovimientoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMotivosMovimiento(motivos: List<MotivoMovimientoEntity>)

    @Query("SELECT * FROM motivo_movimientos")
    fun getAllMotivosMovimiento(): Flow<List<MotivoMovimientoEntity>>
}