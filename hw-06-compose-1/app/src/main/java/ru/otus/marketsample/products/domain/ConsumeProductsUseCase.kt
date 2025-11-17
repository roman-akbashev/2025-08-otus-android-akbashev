package ru.otus.marketsample.products.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.otus.common.data.products.ProductRepository
import ru.otus.common.data.promo.PromoRepository
import javax.inject.Inject

class ConsumeProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val promoRepository: PromoRepository,
    private val productDomainMapper: ProductDomainMapper,
) {
    operator fun invoke(): Flow<List<Product>> {
        return combine(
            productRepository.consumeProducts(),
            promoRepository.consumePromos(),
        ) { products, promos ->
            products.map { product ->
                productDomainMapper.fromEntity(product, promos)
            }
        }
    }
}