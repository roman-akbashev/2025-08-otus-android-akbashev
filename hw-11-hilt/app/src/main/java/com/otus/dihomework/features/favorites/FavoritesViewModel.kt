package com.otus.dihomework.features.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otus.dihomework.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoritesViewModel() : ViewModel() {

    private val consumeFavoritesUseCase = ServiceLocator.getConsumeFavoritesUseCase()
    private val toggleFavoriteUseCase = ServiceLocator.getToggleFavoriteUseCase()
    private val favoritesStateFactory = ServiceLocator.getFavoritesStateFactory()

    private val _state = MutableStateFlow(FavoritesScreenState())
    val state: StateFlow<FavoritesScreenState> = _state.asStateFlow()

    init {
        loadFavorites()
    }

    fun onRemoveFavorite(productId: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(productId, false)
        }
    }

    private fun loadFavorites() {
        consumeFavoritesUseCase()
            .map { favorites -> favoritesStateFactory.create(favorites) }
            .onEach { favoriteStates ->
                _state.update {
                    it.copy(
                        favorites = favoriteStates,
                        isLoading = false,
                        error = null
                    )
                }
            }
            .catch { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error"
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
