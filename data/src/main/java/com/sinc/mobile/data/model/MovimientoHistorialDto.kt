package com.sinc.mobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovimientoHistorialDto(
    @SerialName("id")
    val id: Long,
    @SerialName("fecha_registro")
    val fechaRegistro: String,
    @SerialName("cantidad")
    val cantidad: Int,
    @SerialName("especie")
    val especie: String,
    @SerialName("categoria")
    val categoria: String,
    @SerialName("raza")
    val raza: String,
    @SerialName("motivo")
    val motivo: String,
    @SerialName("tipo_movimiento")
    val tipoMovimiento: String,
    @SerialName("unidad_productiva")
    val unidadProductiva: String,
    @SerialName("destino_traslado")
    val destinoTraslado: String?
)
