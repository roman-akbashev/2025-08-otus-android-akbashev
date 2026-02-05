package com.otus.dihomework.common.data

import retrofit2.http.GET

interface ProductApiService {
    @GET("/static/compose-hw1/products.json")
    suspend fun getProducts(): List<ProductDto>
}
