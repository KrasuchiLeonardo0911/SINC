package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinc.mobile.data.local.entities.TipoPastoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoPastoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTiposPasto(tipos: List<TipoPastoEntity>)

    @Query("SELECT * FROM tipos_pasto")
    fun getAllTiposPasto(): Flow<List<TipoPastoEntity>>
}
