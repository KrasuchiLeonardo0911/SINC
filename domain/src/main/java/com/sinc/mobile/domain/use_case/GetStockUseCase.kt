package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.Stock
import com.sinc.mobile.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStockUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(): Flow<Stock> {
        return repository.getStock()
    }
}
