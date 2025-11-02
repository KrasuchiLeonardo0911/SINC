package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinc.mobile.data.local.entities.RazaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RazaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(razas: List<RazaEntity>)

    @Query("SELECT * FROM razas")
    fun getAllRazas(): Flow<List<RazaEntity>>

    @Query("DELETE FROM razas")
    suspend fun clearAll()
}
