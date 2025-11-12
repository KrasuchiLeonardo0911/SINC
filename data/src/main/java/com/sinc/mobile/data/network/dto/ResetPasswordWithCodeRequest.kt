package com.sinc.mobile.data.network.dto

import com.google.gson.annotations.SerializedName

data class ResetPasswordWithCodeRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("code")
    val code: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("password_confirmation")
    val passwordConfirmation: String
)
