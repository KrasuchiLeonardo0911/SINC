package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sinc.mobile.data.local.entities.agenda.AgendaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgendaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgendaItem(item: AgendaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AgendaEntity>)

    @Query("SELECT * FROM agenda_items ORDER BY fechaProgramada ASC")
    fun getAllAgendaItems(): Flow<List<AgendaEntity>>

    @Query("SELECT * FROM agenda_items WHERE id = :id")
    fun getAgendaItemById(id: Long): Flow<AgendaEntity?>

    @Query("DELETE FROM agenda_items WHERE id = :id")
    suspend fun deleteAgendaItemById(id: Long)

    @Query("DELETE FROM agenda_items")
    suspend fun deleteAll()

    @Transaction
    suspend fun clearAndInsert(items: List<AgendaEntity>) {
        deleteAll()
        insertAll(items)
    }
}
