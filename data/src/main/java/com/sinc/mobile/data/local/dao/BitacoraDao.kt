package com.sinc.mobile.data.local.dao

import androidx.room.*
import com.sinc.mobile.data.local.entities.BitacoraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BitacoraDao {
    @Query("SELECT * FROM bitacoras ORDER BY fecha DESC")
    fun getAllBitacoras(): Flow<List<BitacoraEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bitacoras: List<BitacoraEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bitacora: BitacoraEntity)

    @Query("DELETE FROM bitacoras")
    suspend fun clearAll()

    @Query("DELETE FROM bitacoras WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Transaction
    suspend fun clearAndInsert(bitacoras: List<BitacoraEntity>) {
        clearAll()
        insertAll(bitacoras)
    }
}
