package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinc.mobile.data.local.entities.MunicipioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MunicipioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMunicipios(municipios: List<MunicipioEntity>)

    @Query("SELECT * FROM municipios")
    fun getAllMunicipios(): Flow<List<MunicipioEntity>>
}
