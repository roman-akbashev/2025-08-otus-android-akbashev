package com.otus.dihomework.features.products

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

class ProductsViewModel() : ViewModel() {

    private val consumeProductsUseCase = ServiceLocator.getConsumeProductsUseCase()
    private val toggleFavoriteUseCase = ServiceLocator.getToggleFavoriteUseCase()
    private val productsStateFactory = ServiceLocator.getProductsStateFactory()

    private val _state = MutableStateFlow(ProductsScreenState())
    val state: StateFlow<ProductsScreenState> = _state.asStateFlow()

    init {
        loadProducts()
    }

    fun onToggleFavorite(productId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            toggleFavoriteUseCase(productId, isFavorite)
        }
    }

    private fun loadProducts() {
        consumeProductsUseCase()
            .map { products -> productsStateFactory.create(products) }
            .onEach { productStates ->
                _state.update {
                    it.copy(
                        products = productStates,
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
