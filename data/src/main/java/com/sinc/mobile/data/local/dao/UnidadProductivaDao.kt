package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinc.mobile.data.local.entities.UnidadProductivaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnidadProductivaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(unidades: List<UnidadProductivaEntity>)

    @Query("SELECT * FROM unidades_productivas")
    fun getAllUnidadesProductivas(): Flow<List<UnidadProductivaEntity>>

    @Query("DELETE FROM unidades_productivas")
    suspend fun clearAll()
}
