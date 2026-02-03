package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey val id: Long,
    val userId: Long,
    val solicitableId: Long?,
    val solicitableType: String?,
    val tipo: String,
    val status: String?,
    val asunto: String,
    val responderId: Long?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val responderName: String?
)
