package com.sinc.mobile.domain.use_case.ventas

import com.sinc.mobile.domain.repository.AuthRepository
import javax.inject.Inject

class GetLogisticsStatusUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Boolean {
        return authRepository.isLogisticsOpen()
    }
}
