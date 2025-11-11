package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.AuthResult

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult
    suspend fun logout(): Result<Unit>
}
