package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.UnidadProductiva

import kotlinx.coroutines.flow.Flow

interface UnidadProductivaRepository {
    fun getUnidadesProductivas(): Flow<List<UnidadProductiva>>
    suspend fun syncUnidadesProductivas(): Result<Unit>
}
