package com.sinc.mobile.domain.use_case.profile

import com.sinc.mobile.domain.model.User
import com.sinc.mobile.domain.repository.AuthRepository
import com.sinc.mobile.domain.util.Error
import com.sinc.mobile.domain.util.Result
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User, Error> {
        return authRepository.getUserProfile()
    }
}
