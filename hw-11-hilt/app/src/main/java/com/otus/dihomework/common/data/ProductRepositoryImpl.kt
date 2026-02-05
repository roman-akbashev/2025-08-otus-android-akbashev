package com.otus.dihomework.common.data

import com.otus.dihomework.ServiceLocator
import com.otus.dihomework.common.domain_impl.ProductRepository
import com.otus.dihomework.common.domain_api.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl() : ProductRepository {

    private val remoteDataSource = ServiceLocator.getProductRemoteDataSource()
    private val mapper = ServiceLocator.getProductDomainMapper()

    override fun consumeProducts(): Flow<List<Product>> {
        return remoteDataSource.consumeProducts()
            .map { dtos -> dtos.map(mapper::fromDto) }
    }
}
