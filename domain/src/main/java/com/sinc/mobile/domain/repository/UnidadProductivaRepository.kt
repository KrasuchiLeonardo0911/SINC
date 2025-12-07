package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.CreateUnidadProductivaData
import com.sinc.mobile.domain.model.UnidadProductiva
import com.sinc.mobile.domain.util.Error
import com.sinc.mobile.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface UnidadProductivaRepository {
    fun getUnidadesProductivas(): Flow<List<UnidadProductiva>>
    suspend fun syncUnidadesProductivas(): Result<Unit, Error>
    suspend fun createUnidadProductiva(data: CreateUnidadProductivaData): Result<UnidadProductiva, Error>
}