package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.ChangePasswordData
import com.sinc.mobile.domain.repository.AuthRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(passwordData: ChangePasswordData): Result<Unit> {
        if (passwordData.currentPassword.isBlank() || passwordData.newPassword.isBlank() || passwordData.newPasswordConfirmation.isBlank()) {
            return Result.failure(Exception("Todos los campos son obligatorios."))
        }
        if (passwordData.newPassword != passwordData.newPasswordConfirmation) {
            return Result.failure(Exception("La nueva contraseña y su confirmación no coinciden."))
        }
        if (passwordData.newPassword.length < 8) {
            return Result.failure(Exception("La nueva contraseña debe tener al menos 8 caracteres."))
        }

        return authRepository.changePassword(passwordData)
    }
}
