package com.sinc.mobile.domain.model

import java.time.LocalDateTime

data class Bitacora(
    val id: Int = 0,
    val userId: Int = 0,
    val fecha: LocalDateTime,
    val contenido: String,
    val isSynced: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
