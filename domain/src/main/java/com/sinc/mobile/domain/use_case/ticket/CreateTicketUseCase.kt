package com.sinc.mobile.domain.use_case.ticket

import com.sinc.mobile.domain.model.ticket.CreateTicketData
import com.sinc.mobile.domain.model.ticket.Ticket
import com.sinc.mobile.domain.repository.TicketRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class CreateTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository
) {
    suspend operator fun invoke(data: CreateTicketData): Result<Ticket, Error> {
        return ticketRepository.createTicket(data)
    }
}
