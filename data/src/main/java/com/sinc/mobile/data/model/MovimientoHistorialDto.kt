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
    @SerialName("especie_id")
    val especieId: Int = 0,
    @SerialName("categoria")
    val categoria: String,
    @SerialName("categoria_id")
    val categoriaId: Int = 0,
    @SerialName("raza")
    val raza: String,
    @SerialName("raza_id")
    val razaId: Int = 0,
    @SerialName("motivo")
    val motivo: String,
    @SerialName("motivo_id")
    val motivoId: Int = 0,
    @SerialName("tipo_movimiento")
    val tipoMovimiento: String,
    @SerialName("unidad_productiva")
    val unidadProductiva: String,
    @SerialName("unidad_productiva_id")
    val unidadProductivaId: Int = 0,
    @SerialName("destino_traslado")
    val destinoTraslado: String?
)
