package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.IdentifierConfig
import com.sinc.mobile.domain.repository.IdentifierRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetIdentifierConfigsUseCase @Inject constructor(
    private val identifierRepository: IdentifierRepository
) {
    operator fun invoke(): Flow<List<IdentifierConfig>> {
        return identifierRepository.getIdentifierConfigs()
    }
}
