package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinc.mobile.data.local.entities.FuenteAguaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuenteAguaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFuentesAgua(fuentes: List<FuenteAguaEntity>)

    @Query("SELECT * FROM fuentes_agua")
    fun getAllFuentesAgua(): Flow<List<FuenteAguaEntity>>
}
