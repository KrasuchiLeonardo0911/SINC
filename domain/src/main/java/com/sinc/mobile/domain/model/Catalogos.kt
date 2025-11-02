package com.sinc.mobile.domain.model

data class Catalogos(
    val especies: List<Especie>,
    val razas: List<Raza>,
    val categorias: List<Categoria>,
    val motivosMovimiento: List<MotivoMovimiento>
)

data class Especie(
    val id: Int,
    val nombre: String
)

data class Raza(
    val id: Int,
    val nombre: String,
    val especieId: Int
)

data class Categoria(
    val id: Int,
    val nombre: String,
    val especieId: Int
)

data class MotivoMovimiento(
    val id: Int,
    val nombre: String,
    val tipo: String
)
