package com.otus.dihomework.features.favorites

import com.otus.dihomework.ServiceLocator
import com.otus.dihomework.common.domain_api.ProductWithFavorite

class FavoritesStateFactory() {
    private val priceFormatter = ServiceLocator.getPriceFormatter()

    fun create(favorites: List<ProductWithFavorite>): List<FavoriteProductState> {
        return favorites.map { createFavoriteState(it) }
    }

    private fun createFavoriteState(productWithFavorite: ProductWithFavorite): FavoriteProductState {
        return FavoriteProductState(
            id = productWithFavorite.product.id,
            name = productWithFavorite.product.name,
            imageUrl = productWithFavorite.product.imageUrl,
            price = priceFormatter.format(productWithFavorite.product.price)
        )
    }
}
