package com.sinc.mobile.domain.model.ticket

import java.time.LocalDateTime

data class Ticket(
    val id: Long,
    val userId: Long,
    val solicitableId: Long?,
    val solicitableType: String?,
    val tipo: String,
    val status: String?,
    val asunto: String,
    val responderId: Long?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val responderName: String?,
    val messages: List<Message>
)
