package com.otus.dihomework.features.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.otus.dihomework.common.domain_api.ConsumeFavoritesUseCase
import com.otus.dihomework.common.domain_api.ToggleFavoriteUseCase
import javax.inject.Inject

class FavoritesViewModelFactory @Inject constructor(
    private val consumeFavoritesUseCase: ConsumeFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val favoritesStateFactory: FavoritesStateFactory
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == FavoritesViewModel::class.java)
        return FavoritesViewModel(
            consumeFavoritesUseCase,
            toggleFavoriteUseCase,
            favoritesStateFactory
        ) as T
    }
}