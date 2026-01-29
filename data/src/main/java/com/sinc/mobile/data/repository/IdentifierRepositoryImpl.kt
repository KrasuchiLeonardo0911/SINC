package com.sinc.mobile.data.repository

import com.sinc.mobile.data.local.dao.IdentifierConfigDao
import com.sinc.mobile.data.local.entities.IdentifierConfigEntity
import com.sinc.mobile.data.network.IdentifierApiService
import com.sinc.mobile.data.network.dto.IdentifierConfigDto
import com.sinc.mobile.domain.model.IdentifierConfig
import com.sinc.mobile.domain.repository.IdentifierRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentifierRepositoryImpl @Inject constructor(
    private val identifierApiService: IdentifierApiService,
    private val identifierConfigDao: IdentifierConfigDao
) : IdentifierRepository {

    override fun getIdentifierConfigs(): Flow<List<IdentifierConfig>> {
        return identifierConfigDao.getIdentifierConfigs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncIdentifierConfigs(): Result<Unit> {
        return try {
            val response = identifierApiService.getIdentifierConfigs()
            identifierConfigDao.clearAll()
            identifierConfigDao.insertAll(response.data.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun IdentifierConfigEntity.toDomain(): IdentifierConfig {
        return IdentifierConfig(
            type = type,
            label = label,
            hint = hint,
            regex = regex
        )
    }

    private fun IdentifierConfigDto.toEntity(): IdentifierConfigEntity {
        return IdentifierConfigEntity(
            type = type,
            label = label,
            hint = hint ?: "",
            regex = regex
        )
    }
}
