package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.AuthResult
import com.sinc.mobile.domain.model.ChangePasswordData
import com.sinc.mobile.domain.model.RequestPasswordResetData
import com.sinc.mobile.domain.model.ResetPasswordWithCodeData

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult
    suspend fun logout(): Result<Unit>
    suspend fun changePassword(passwordData: ChangePasswordData): Result<Unit>
    suspend fun requestPasswordReset(data: RequestPasswordResetData): Result<Unit>
    suspend fun resetPasswordWithCode(data: ResetPasswordWithCodeData): Result<Unit>
}
