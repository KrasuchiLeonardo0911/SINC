package com.sinc.mobile.domain.model

data class ChangePasswordData(
    val currentPassword: String,
    val newPassword: String,
    val newPasswordConfirmation: String
)
