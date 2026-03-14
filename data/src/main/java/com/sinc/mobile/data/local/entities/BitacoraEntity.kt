package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "bitacoras")
data class BitacoraEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val id: Int? = null, // ID del servidor
    val userId: Int,
    val fecha: LocalDateTime,
    val contenido: String,
    val sincronizado: Boolean = false,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
