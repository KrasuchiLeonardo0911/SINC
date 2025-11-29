package com.sinc.mobile.domain.model

data class Municipio(
    val id: Int,
    val nombre: String,
    val centroide: DomainGeoPoint? = null,
    val poligono: List<DomainGeoPoint>? = null
)
