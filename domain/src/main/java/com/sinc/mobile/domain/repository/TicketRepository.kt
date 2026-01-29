package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.ticket.CreateTicketData
import com.sinc.mobile.domain.model.ticket.Ticket
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    fun getTickets(): Flow<List<Ticket>>
    suspend fun syncTickets(): Result<Unit, Error>
    suspend fun createTicket(data: CreateTicketData): Result<Ticket, Error>
    suspend fun addMessage(ticketId: Long, message: String): Result<Ticket, Error>
    suspend fun syncTicket(ticketId: Long): Result<Unit, Error>
}