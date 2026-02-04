package com.sinc.mobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductorDto(
    @SerialName("id") val id: Int,
    @SerialName("nombre") val nombre: String,
    @SerialName("dni") val dni: String? = null,
    @SerialName("cuil") val cuil: String? = null,
    @SerialName("fecha_nacimiento") val fechaNacimiento: String? = null,
    @SerialName("telefono") val telefono: String? = null,
    @SerialName("direccion") val direccion: String? = null,
    @SerialName("paraje") val paraje: String? = null
)
