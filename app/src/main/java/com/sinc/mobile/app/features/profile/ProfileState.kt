package com.sinc.mobile.app.features.profile

import com.sinc.mobile.domain.model.User

data class ProfileState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null
)
