package com.sinc.mobile.domain.use_case.ticket

import com.sinc.mobile.domain.model.ticket.Ticket
import com.sinc.mobile.domain.repository.TicketRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class AddMessageToTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository
) {
    suspend operator fun invoke(ticketId: Long, message: String): Result<Ticket, Error> {
        if (message.isBlank()) {
            return Result.Failure(object : Error {
                override val message: String = "El mensaje no puede estar vacío."
            })
        }
        return ticketRepository.addMessage(ticketId, message)
    }
}
