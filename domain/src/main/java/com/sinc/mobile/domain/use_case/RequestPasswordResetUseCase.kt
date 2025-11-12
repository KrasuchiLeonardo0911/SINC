package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.RequestPasswordResetData
import com.sinc.mobile.domain.repository.AuthRepository
import javax.inject.Inject

// Regex for email validation, adapted from Android's Patterns.EMAIL_ADDRESS
private val EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$".toRegex()

class RequestPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (!EMAIL_REGEX.matches(email)) {
            return Result.failure(Exception("Por favor, introduce una dirección de correo válida."))
        }
        return authRepository.requestPasswordReset(RequestPasswordResetData(email))
    }
}
