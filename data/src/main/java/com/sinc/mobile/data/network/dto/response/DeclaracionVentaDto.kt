package com.sinc.mobile.data.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeclaracionVentaDto(
    @SerialName("id") val id: Int,
    @SerialName("productor_id") val productorId: Int,
    @SerialName("unidad_productiva_id") val unidadProductivaId: Int,
    @SerialName("especie_id") val especieId: Int,
    @SerialName("raza_id") val razaId: Int,
    @SerialName("categoria_animal_id") val categoriaAnimalId: Int,
    @SerialName("cantidad") val cantidad: Int,
    @SerialName("estado") val estado: String,
    @SerialName("fecha_declaracion") val fechaDeclaracion: String,
    @SerialName("observaciones") val observaciones: String? = null,
    @SerialName("peso_aproximado_kg") val pesoAproximadoKg: Float? = null
)
