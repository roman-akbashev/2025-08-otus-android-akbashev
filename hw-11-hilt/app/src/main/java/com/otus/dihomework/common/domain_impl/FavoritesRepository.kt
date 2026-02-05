package com.otus.dihomework.common.domain_impl

import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun consumeFavoriteIds(): Flow<Set<String>>
    suspend fun addToFavorites(productId: String)
    suspend fun removeFromFavorites(productId: String)
}
