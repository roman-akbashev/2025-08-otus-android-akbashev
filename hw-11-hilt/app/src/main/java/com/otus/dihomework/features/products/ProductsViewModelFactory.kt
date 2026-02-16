package com.otus.dihomework.features.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.otus.dihomework.common.domain_api.ConsumeProductsUseCase
import com.otus.dihomework.common.domain_api.ToggleFavoriteUseCase
import javax.inject.Inject

class ProductsViewModelFactory @Inject constructor(
    private val consumeProductsUseCase: ConsumeProductsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val productsStateFactory: ProductsStateFactory
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ProductsViewModel::class.java)
        return ProductsViewModel(
            consumeProductsUseCase,
            toggleFavoriteUseCase,
            productsStateFactory
        ) as T
    }
}