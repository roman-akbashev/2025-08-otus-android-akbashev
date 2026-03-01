package com.otus.dihomework.features.products

import com.otus.dihomework.common.domain_api.ProductWithFavorite
import com.otus.dihomework.common.util.PriceFormatter
import javax.inject.Inject

class ProductsStateFactory @Inject constructor(
    private val priceFormatter: PriceFormatter
) {

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