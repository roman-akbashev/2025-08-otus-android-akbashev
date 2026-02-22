package com.otus.dihomework.common.domain_impl

import com.otus.dihomework.common.domain_api.ConsumeFavoritesUseCase
import com.otus.dihomework.common.domain_api.ProductWithFavorite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ConsumeFavoritesUseCaseImpl @Inject constructor(
    private val productRepository: ProductRepository,
    private val favoritesRepository: FavoritesRepository
) : ConsumeFavoritesUseCase {

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