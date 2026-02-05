package com.otus.dihomework.features.products

data class ProductsScreenState(
    val products: List<ProductState> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class ProductState(
    val id: String,
    val name: String,
    val imageUrl: String,
    val price: String,
    val isFavorite: Boolean
)
