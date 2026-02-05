package com.otus.dihomework.common.domain_api

import kotlinx.coroutines.flow.Flow

interface ConsumeProductsUseCase {
    operator fun invoke(): Flow<List<ProductWithFavorite>>
}
