package com.sinc.mobile.data.local.entities.agenda

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "agenda_items")
data class AgendaEntity(
    @PrimaryKey val id: Long,
    val userId: Long,
    val titulo: String,
    val descripcion: String?,
    val tipo: String,
    val fechaProgramada: LocalDateTime,
    val completadaEn: LocalDateTime?,
    val notificado: Boolean,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)
