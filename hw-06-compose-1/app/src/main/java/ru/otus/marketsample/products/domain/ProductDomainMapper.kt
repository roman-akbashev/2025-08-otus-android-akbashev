package ru.otus.marketsample.products.domain

import ru.otus.common.data.products.ProductEntity
import ru.otus.common.data.promo.PromoEntity
import javax.inject.Inject

class ProductDomainMapper @Inject constructor() {
    fun fromEntity(productEntity: ProductEntity, promos: List<PromoEntity>): Product {
        val promoForProduct: PromoEntity? = promos.firstOrNull { promoEntity ->
            (promoEntity.type == "product" &&
                    promoEntity.products.any { productId -> productId == productEntity.id })
        }
        return Product(
            id = productEntity.id,
            name = productEntity.name,
            image = productEntity.image,
            price = productEntity.price,
            hasDiscount = promoForProduct != null,
            discount = promoForProduct.resolveDiscount(),
        )
    }

    private fun PromoEntity?.resolveDiscount(): Double? {
        if (this == null) { return null }

        return if (type == "product") {
            discount
        } else {
            null
        }
    }
}
