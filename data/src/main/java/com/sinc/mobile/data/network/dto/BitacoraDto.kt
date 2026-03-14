package com.sinc.mobile.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class BitacoraDto(
    val id: Int,
    val user_id: Int,
    val fecha: String,
    val contenido: String,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class CreateBitacoraRequest(
    val fecha: String,
    val contenido: String
)

@Serializable
data class UpdateBitacoraRequest(
    val contenido: String
)
