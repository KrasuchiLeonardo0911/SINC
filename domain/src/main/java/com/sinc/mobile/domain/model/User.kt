package com.sinc.mobile.domain.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val productor: Productor?
)
