package com.sinc.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unidades_productivas")
data class UnidadProductivaEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val identificadorLocal: String?,
    val superficie: Float,
    val latitud: Double?,
    val longitud: Double?,
    val municipioId: Int,
    val condicionTenenciaId: Int?,
    val aguaHumanoFuenteId: Int?,
    val aguaHumanoEnCasa: Boolean?,
    val aguaHumanoDistancia: Int?,
    val aguaAnimalFuenteId: Int?,
    val aguaAnimalDistancia: Int?,
    val tipoSueloId: Int?,
    val tipoPastoId: Int?,
    val forrajerasPredominante: Boolean?,
    val habita: Boolean?,
    val observaciones: String? = null,
    val activo: Boolean = false,
    val completo: Boolean = false
)