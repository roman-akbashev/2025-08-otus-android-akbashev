package ru.otus.marketsample.details.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.otus.common.data.products.ProductRepository
import javax.inject.Inject

class ConsumeProductDetailsUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val productDetailsDomainMapper: ProductDetailsDomainMapper,
) {
    operator fun invoke(productId: String): Flow<ProductDetails> {
        return productRepository.consumeProducts()
            .map { products ->
                products
                    .first { it.id == productId }
                    .run(productDetailsDomainMapper::fromEntity)
            }
    }
}