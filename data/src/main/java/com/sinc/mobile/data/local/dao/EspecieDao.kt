package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinc.mobile.data.local.entities.EspecieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EspecieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEspecies(especies: List<EspecieEntity>)

    @Query("SELECT * FROM especies")
    fun getAllEspecies(): Flow<List<EspecieEntity>>
}