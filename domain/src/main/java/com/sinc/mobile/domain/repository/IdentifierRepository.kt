package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.IdentifierConfig
import kotlinx.coroutines.flow.Flow

interface IdentifierRepository {
    fun getIdentifierConfigs(): Flow<List<IdentifierConfig>>
    suspend fun syncIdentifierConfigs(): Result<Unit>
}
