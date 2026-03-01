package com.otus.dihomework.features.favorites

import com.otus.dihomework.common.domain_api.ProductWithFavorite
import com.otus.dihomework.common.util.PriceFormatter
import javax.inject.Inject

class FavoritesStateFactory @Inject constructor(
    private val priceFormatter: PriceFormatter
) {

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