package ru.otus.marketsample.details.domain

import ru.otus.common.data.products.ProductEntity
import javax.inject.Inject

class ProductDetailsDomainMapper @Inject constructor() {
    fun fromEntity(productEntity: ProductEntity): ProductDetails {
        return ProductDetails(
            id = productEntity.id,
            name = productEntity.name,
            image = productEntity.image,
            price = productEntity.price
        )
    }
}
