package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.ticket.CreateTicketData
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error

interface TicketRepository {
    suspend fun createTicket(ticketData: CreateTicketData): Result<Unit, Error>
}
