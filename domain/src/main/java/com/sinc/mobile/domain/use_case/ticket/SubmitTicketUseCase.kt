package com.sinc.mobile.domain.use_case.ticket

import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.model.ticket.CreateTicketData
import com.sinc.mobile.domain.repository.TicketRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class SubmitTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository
) {
    suspend operator fun invoke(mensaje: String, tipo: String): Result<Unit, Error> {
        if (mensaje.isBlank()) {
            return Result.Failure(GenericError("El mensaje no puede estar vacío."))
        }
        val ticketData = CreateTicketData(mensaje = mensaje, tipo = tipo)
        return ticketRepository.createTicket(ticketData)
    }
}
