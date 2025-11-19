package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinc.mobile.data.local.entities.TipoSueloEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoSueloDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTiposSuelo(tipos: List<TipoSueloEntity>)

    @Query("SELECT * FROM tipos_suelo")
    fun getAllTiposSuelo(): Flow<List<TipoSueloEntity>>
}
