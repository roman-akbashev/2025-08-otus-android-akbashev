package ru.otus.marketsample.products.feature

import ru.otus.common.di.FeatureScope
import ru.otus.marketsample.products.domain.Product
import ru.otus.common.formatters.DiscountFormatter
import ru.otus.common.formatters.PriceFormatter
import javax.inject.Inject

@FeatureScope
class ProductStateFactory @Inject constructor(
    private val discountFormatter: DiscountFormatter,
    private val priceFormatter: PriceFormatter,
) {
    fun create(product: Product): ProductState {
        return ProductState(
            id = product.id,
            name = product.name,
            image = product.image,
            price = product.price.let(priceFormatter::format),
            hasDiscount = product.hasDiscount,
            discount = product.resolveDiscount(),
        )
    }

    private fun Product.resolveDiscount(): String {
        return discount
            ?.toInt()
            ?.let(discountFormatter::format)
            ?: ""
    }
}