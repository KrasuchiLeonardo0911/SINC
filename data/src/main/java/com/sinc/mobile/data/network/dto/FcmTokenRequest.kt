package com.sinc.mobile.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class FcmTokenRequest(
    val token: String
)
