package com.sinc.mobile.domain.model

data class ResetPasswordWithCodeData(
    val email: String,
    val code: String,
    val password: String,
    val passwordConfirmation: String
)
