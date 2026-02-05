package com.otus.dihomework.features.favorites

data class FavoritesScreenState(
    val favorites: List<FavoriteProductState> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class FavoriteProductState(
    val id: String,
    val name: String,
    val imageUrl: String,
    val price: String
)
