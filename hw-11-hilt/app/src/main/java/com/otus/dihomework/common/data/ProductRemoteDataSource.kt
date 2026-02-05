package com.otus.dihomework.common.data

import com.otus.dihomework.ServiceLocator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProductRemoteDataSource() {
    private val apiService = ServiceLocator.getProductApiService()

    fun consumeProducts(): Flow<List<ProductDto>> = flow {
        emit(apiService.getProducts())
    }
}
