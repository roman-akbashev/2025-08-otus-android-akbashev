package com.otus.dihomework.features.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otus.dihomework.common.domain_api.ConsumeProductsUseCase
import com.otus.dihomework.common.domain_api.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProductsViewModel @Inject constructor(
    private val consumeProductsUseCase: ConsumeProductsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val productsStateFactory: ProductsStateFactory
) : ViewModel() {

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