package com.otus.dihomework.common.domain_impl

import com.otus.dihomework.ServiceLocator
import com.otus.dihomework.common.domain_api.ConsumeFavoritesUseCase
import com.otus.dihomework.common.domain_api.ProductWithFavorite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ConsumeFavoritesUseCaseImpl() : ConsumeFavoritesUseCase {

    private val productRepository = ServiceLocator.getProductRepository()
    private val favoritesRepository = ServiceLocator.getFavoritesRepository()

    override fun invoke(): Flow<List<ProductWithFavorite>> {
        return combine(
            productRepository.consumeProducts(),
            favoritesRepository.consumeFavoriteIds()
        ) { products, favoriteIds ->
            products
                .filter { product -> product.id in favoriteIds }
                .map { product ->
                    ProductWithFavorite(
                        product = product,
                        isFavorite = true
                    )
                }
        }
    }
}
