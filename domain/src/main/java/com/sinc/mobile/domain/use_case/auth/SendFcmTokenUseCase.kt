package com.sinc.mobile.domain.use_case.auth

import com.sinc.mobile.domain.repository.AuthRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class SendFcmTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(token: String): Result<Unit, Error> {
        if (token.isBlank()) {
            // Optional: Add some basic validation
            return Result.Failure(object : Error {
                override val message: String = "El token FCM no puede estar vacío."
            })
        }
        return authRepository.sendFcmToken(token)
    }
}
