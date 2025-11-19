package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinc.mobile.data.local.entities.CondicionTenenciaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CondicionTenenciaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCondicionesTenencia(condiciones: List<CondicionTenenciaEntity>)

    @Query("SELECT * FROM condiciones_tenencia")
    fun getAllCondicionesTenencia(): Flow<List<CondicionTenenciaEntity>>
}
