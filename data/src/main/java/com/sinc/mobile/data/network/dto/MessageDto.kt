package com.sinc.mobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    @SerialName("id") val id: Long,
    @SerialName("user_id") val userId: Long,
    @SerialName("message") val message: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("user") val user: UserDto
)
