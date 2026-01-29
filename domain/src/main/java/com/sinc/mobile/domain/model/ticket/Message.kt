package com.sinc.mobile.domain.model.ticket

import java.time.LocalDateTime

data class Message(
    val id: Long,
    val ticketId: Long,
    val userId: Long,
    val message: String,
    val createdAt: LocalDateTime,
    val userName: String,
    val isFromUser: Boolean
)
