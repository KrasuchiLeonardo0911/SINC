package com.sinc.mobile.data.mapper

import com.sinc.mobile.data.network.dto.ProductorDto
import com.sinc.mobile.data.network.dto.UserDto
import com.sinc.mobile.domain.model.Productor
import com.sinc.mobile.domain.model.User

fun UserDto.toDomain(): User {
    return User(
        id = this.id,
        name = this.name,
        email = this.email,
        productor = this.productor?.toDomain()
    )
}

fun ProductorDto.toDomain(): Productor {
    return Productor(
        id = this.id,
        nombre = this.nombre,
        dni = this.dni,
        cuil = this.cuil,
        fechaNacimiento = this.fechaNacimiento,
        telefono = this.telefono,
        direccion = this.direccion,
        paraje = this.paraje
    )
}
