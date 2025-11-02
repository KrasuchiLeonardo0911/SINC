package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.UnidadProductiva

interface UnidadProductivaRepository {
    suspend fun getUnidadesProductivas(): Result<List<UnidadProductiva>>
}
