package com.otus.dihomework.features.products

import com.otus.dihomework.ServiceLocator
import com.otus.dihomework.common.domain_api.ProductWithFavorite

class ProductsStateFactory() {
    private val priceFormatter = ServiceLocator.getPriceFormatter()

    fun create(products: List<ProductWithFavorite>): List<ProductState> {
        return products
            .map { createProductState(it) }
    }

    private fun createProductState(productWithFavorite: ProductWithFavorite): ProductState {
        return ProductState(
            id = productWithFavorite.product.id,
            name = productWithFavorite.product.name,
            imageUrl = productWithFavorite.product.imageUrl,
            price = priceFormatter.format(productWithFavorite.product.price),
            isFavorite = productWithFavorite.isFavorite
        )
    }
}
