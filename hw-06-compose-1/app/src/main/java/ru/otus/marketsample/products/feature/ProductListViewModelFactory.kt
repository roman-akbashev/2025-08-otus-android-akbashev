package ru.otus.marketsample.products.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import ru.otus.common.di.FeatureScope
import ru.otus.marketsample.products.domain.ConsumeProductsUseCase
import javax.inject.Inject

@FeatureScope
class ProductListViewModelFactory @Inject constructor(
    private val consumeProductsUseCase: ConsumeProductsUseCase,
    private val productStateFactory: ProductStateFactory,
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras,
    ): T {
        when {
            modelClass.isAssignableFrom(ProductListViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return ProductListViewModel(
                    consumeProductsUseCase = consumeProductsUseCase,
                    productStateFactory = productStateFactory,
                ) as T
            }
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}