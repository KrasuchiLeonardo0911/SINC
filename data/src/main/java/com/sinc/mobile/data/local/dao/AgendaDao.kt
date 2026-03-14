package com.sinc.mobile.data.local.dao

import androidx.room.*
import com.sinc.mobile.data.local.entities.agenda.AgendaEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface AgendaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgendaItem(item: AgendaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AgendaEntity>)

    @Query("SELECT * FROM agenda_items ORDER BY fechaProgramada ASC")
    fun getAllAgendaItems(): Flow<List<AgendaEntity>>

    @Query("SELECT * FROM agenda_items")
    suspend fun getAllAgendaItemsOnce(): List<AgendaEntity>

    @Query("SELECT * FROM agenda_items WHERE localId = :id")
    fun getAgendaItemById(id: Long): Flow<AgendaEntity?>

    @Query("SELECT * FROM agenda_items WHERE localId = :id")
    suspend fun getAgendaItemByLocalId(id: Long): AgendaEntity?

    @Query("SELECT * FROM agenda_items WHERE sincronizado = 0")
    suspend fun getUnsyncedAgendaItems(): List<AgendaEntity>

    @Query("UPDATE agenda_items SET id = :serverId, sincronizado = 1 WHERE localId = :localId")
    suspend fun markAsSynced(localId: Long, serverId: Long)

    @Query("DELETE FROM agenda_items WHERE localId = :id")
    suspend fun deleteAgendaItemByLocalId(id: Long)

    @Query("DELETE FROM agenda_items")
    suspend fun deleteAll()

    @Query("UPDATE agenda_items SET completadaEn = :completadaEn, sincronizado = 0 WHERE localId = :id")
    suspend fun updateStatus(id: Long, completadaEn: LocalDateTime?)

    @Transaction
    suspend fun clearAndInsert(items: List<AgendaEntity>) {
        deleteSyncedAgendaItems()
        insertAll(items)
    }

    @Query("DELETE FROM agenda_items WHERE sincronizado = 1")
    suspend fun deleteSyncedAgendaItems()
}
