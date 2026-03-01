package com.otus.dihomework.common.data

import com.otus.dihomework.common.domain_api.Product
import javax.inject.Inject

class ProductDomainMapper @Inject constructor() {

    fun fromDto(dto: ProductDto): Product {
        return Product(
            id = dto.id,
            name = dto.name,
            imageUrl = dto.image,
            price = dto.price
        )
    }
}
