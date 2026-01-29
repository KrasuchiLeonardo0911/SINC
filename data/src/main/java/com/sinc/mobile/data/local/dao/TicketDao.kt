package com.sinc.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sinc.mobile.data.local.entities.MessageEntity
import com.sinc.mobile.data.local.entities.TicketEntity
import com.sinc.mobile.data.local.relation.TicketWithMessages
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {

    @Transaction
    @Query("SELECT * FROM tickets ORDER BY updatedAt DESC")
    fun getAllTicketsWithMessages(): Flow<List<TicketWithMessages>>

    @Transaction
    @Query("SELECT * FROM tickets WHERE id = :ticketId")
    fun getTicketWithMessages(ticketId: Long): Flow<TicketWithMessages>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("DELETE FROM tickets")
    suspend fun clearAllTickets()

    @Query("DELETE FROM ticket_messages")
    suspend fun clearAllMessages()

    @Transaction
    suspend fun clearAndInsert(tickets: List<TicketWithMessages>) {
        clearAllTickets()
        clearAllMessages()
        val ticketEntities = tickets.map { it.ticket }
        val messageEntities = tickets.flatMap { it.messages }
        if (ticketEntities.isNotEmpty()) {
            insertTickets(ticketEntities)
        }
        if (messageEntities.isNotEmpty()) {
            insertMessages(messageEntities)
        }
    }
    
    @Transaction
    suspend fun upsertTicket(ticket: TicketWithMessages) {
        insertTickets(listOf(ticket.ticket))
        insertMessages(ticket.messages)
    }
}
