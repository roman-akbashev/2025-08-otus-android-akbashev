package com.otus.dihomework.common.domain_api

interface ToggleFavoriteUseCase {
    suspend operator fun invoke(productId: String, isFavorite: Boolean)
}
