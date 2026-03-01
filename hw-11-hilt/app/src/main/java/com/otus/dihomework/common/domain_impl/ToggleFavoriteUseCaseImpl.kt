package com.otus.dihomework.common.domain_impl

import com.otus.dihomework.common.domain_api.ToggleFavoriteUseCase
import javax.inject.Inject

class ToggleFavoriteUseCaseImpl @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ToggleFavoriteUseCase {

    override suspend fun invoke(productId: String, isFavorite: Boolean) {
        if (isFavorite) {
            favoritesRepository.addToFavorites(productId)
        } else {
            favoritesRepository.removeFromFavorites(productId)
        }
    }
}