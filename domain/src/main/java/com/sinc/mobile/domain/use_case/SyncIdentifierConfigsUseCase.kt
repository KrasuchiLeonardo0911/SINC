package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.repository.IdentifierRepository
import javax.inject.Inject

class SyncIdentifierConfigsUseCase @Inject constructor(
    private val identifierRepository: IdentifierRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return identifierRepository.syncIdentifierConfigs()
    }
}
