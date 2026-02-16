package com.otus.dihomework.common.domain_impl

import com.otus.dihomework.common.domain_api.ConsumeProductsUseCase
import com.otus.dihomework.common.domain_api.ProductWithFavorite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConsumeProductsUseCaseImpl @Inject constructor(
    private val productRepository: ProductRepository,
    private val favoritesRepository: FavoritesRepository
) : ConsumeProductsUseCase {

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