package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.LogisticsInfo
import com.sinc.mobile.domain.repository.LogisticsRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLogisticsInfoUseCase @Inject constructor(
    private val repository: LogisticsRepository
) {
    operator fun invoke(): Flow<Result<LogisticsInfo, Error>> {
        return repository.getLogisticsInfo()
    }
}
