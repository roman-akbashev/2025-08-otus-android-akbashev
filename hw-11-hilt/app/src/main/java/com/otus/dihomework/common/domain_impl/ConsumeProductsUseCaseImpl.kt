package com.otus.dihomework.common.domain_impl

import com.otus.dihomework.ServiceLocator
import com.otus.dihomework.common.domain_api.ConsumeProductsUseCase
import com.otus.dihomework.common.domain_api.ProductWithFavorite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ConsumeProductsUseCaseImpl() : ConsumeProductsUseCase {

    private val productRepository = ServiceLocator.getProductRepository()
    private val favoritesRepository = ServiceLocator.getFavoritesRepository()

    override fun invoke(): Flow<List<ProductWithFavorite>> {
        return combine(
            productRepository.consumeProducts().map { it.shuffled() },
            favoritesRepository.consumeFavoriteIds()
        ) { products, favoriteIds ->
            products.map { product ->
                ProductWithFavorite(
                    product = product,
                    isFavorite = product.id in favoriteIds
                )
            }
        }
    }
}
