package com.sinc.mobile.domain.model

data class Productor(
    val id: Int,
    val nombre: String,
    val dni: String?,
    val cuil: String?,
    val fechaNacimiento: String?,
    val telefono: String?,
    val direccion: String?,
    val paraje: String?
)
