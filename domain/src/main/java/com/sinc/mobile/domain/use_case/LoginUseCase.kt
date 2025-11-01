package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.AuthResult
import com.sinc.mobile.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.UnknownError("El email y la contraseña no pueden estar vacíos.")
        }
        return repository.login(email, password)
    }
}
