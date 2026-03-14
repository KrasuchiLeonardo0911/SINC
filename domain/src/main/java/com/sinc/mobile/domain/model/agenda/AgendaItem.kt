package com.sinc.mobile.domain.model.agenda

import java.time.LocalDateTime

data class AgendaItem(
    val id: Long = 0,
    val userId: Long,
    val titulo: String,
    val descripcion: String? = null,
    val tipo: String = "general",
    val fechaProgramada: LocalDateTime,
    val completadaEn: LocalDateTime? = null,
    val isCompleted: Boolean = completadaEn != null,
    val notificado: Boolean = false,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
