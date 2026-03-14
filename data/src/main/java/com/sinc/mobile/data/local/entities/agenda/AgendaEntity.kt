package com.sinc.mobile.data.local.entities.agenda

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "agenda_items")
data class AgendaEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val id: Long? = null, // ID del servidor
    val userId: Long,
    val titulo: String,
    val descripcion: String?,
    val tipo: String,
    val fechaProgramada: LocalDateTime,
    val completadaEn: LocalDateTime?,
    val notificado: Boolean,
    val sincronizado: Boolean = false,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)
