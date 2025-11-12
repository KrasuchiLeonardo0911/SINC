package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.ResetPasswordWithCodeData
import com.sinc.mobile.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordWithCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(data: ResetPasswordWithCodeData): Result<Unit> {
        if (data.email.isBlank() || data.code.isBlank() || data.password.isBlank() || data.passwordConfirmation.isBlank()) {
            return Result.failure(Exception("Todos los campos son obligatorios."))
        }
        if (data.password != data.passwordConfirmation) {
            return Result.failure(Exception("La nueva contraseña y su confirmación no coinciden."))
        }
        if (data.password.length < 8) {
            return Result.failure(Exception("La nueva contraseña debe tener al menos 8 caracteres."))
        }
        if (data.code.length != 6) {
            return Result.failure(Exception("El código de verificación debe tener 6 dígitos."))
        }

        return authRepository.resetPasswordWithCode(data)
    }
}
