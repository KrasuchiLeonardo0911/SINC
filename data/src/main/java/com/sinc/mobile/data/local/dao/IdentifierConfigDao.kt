package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sinc.mobile.data.local.entities.IdentifierConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdentifierConfigDao {
    @Query("SELECT * FROM identifier_configs")
    fun getIdentifierConfigs(): Flow<List<IdentifierConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(configs: List<IdentifierConfigEntity>)

    @Query("DELETE FROM identifier_configs")
    suspend fun clearAll()
}
