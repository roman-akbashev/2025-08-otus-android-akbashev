package com.otus.dihomework.common.domain_impl

import com.otus.dihomework.ServiceLocator
import com.otus.dihomework.common.domain_api.ToggleFavoriteUseCase

class ToggleFavoriteUseCaseImpl() : ToggleFavoriteUseCase {

    private val favoritesRepository = ServiceLocator.getFavoritesRepository()

    override suspend fun invoke(productId: String, isFavorite: Boolean) {
        if (isFavorite) {
            favoritesRepository.addToFavorites(productId)
        } else {
            favoritesRepository.removeFromFavorites(productId)
        }
    }
}
