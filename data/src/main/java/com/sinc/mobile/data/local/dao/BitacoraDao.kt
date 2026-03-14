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
    suspend fun insert(bitacora: BitacoraEntity): Long

    @Query("SELECT * FROM bitacoras WHERE localId = :localId")
    suspend fun getBitacoraByLocalId(localId: Int): BitacoraEntity?

    @Query("SELECT * FROM bitacoras WHERE sincronizado = 0")
    suspend fun getUnsyncedBitacoras(): List<BitacoraEntity>

    @Query("UPDATE bitacoras SET id = :serverId, sincronizado = 1 WHERE localId = :localId")
    suspend fun markAsSynced(localId: Int, serverId: Int)

    @Query("DELETE FROM bitacoras WHERE localId = :localId")
    suspend fun deleteByLocalId(localId: Int)

    @Query("DELETE FROM bitacoras")
    suspend fun clearAll()

    @Transaction
    suspend fun clearAndInsert(bitacoras: List<BitacoraEntity>) {
        // Al sincronizar desde el servidor, solo borramos lo que YA ESTÁ sincronizado
        // para no perder los pendientes locales.
        deleteSyncedBitacoras()
        insertAll(bitacoras)
    }

    @Query("DELETE FROM bitacoras WHERE sincronizado = 1")
    suspend fun deleteSyncedBitacoras()
}
