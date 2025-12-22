package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.repository.StockRepository
import com.sinc.mobile.domain.util.Result
import javax.inject.Inject

class SyncStockUseCase @Inject constructor(
    private val repository: StockRepository
) {
    suspend operator fun invoke(): Result<Unit, GenericError> {
        return repository.syncStock()
    }
}
