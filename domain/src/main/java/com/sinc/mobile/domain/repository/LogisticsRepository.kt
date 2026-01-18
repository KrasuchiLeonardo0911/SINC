package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.LogisticsInfo
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import kotlinx.coroutines.flow.Flow

interface LogisticsRepository {
    fun getLogisticsInfo(): Flow<Result<LogisticsInfo, Error>>
}
