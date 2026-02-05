package com.otus.dihomework.common.domain_impl

import com.otus.dihomework.common.domain_api.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun consumeProducts(): Flow<List<Product>>
}
