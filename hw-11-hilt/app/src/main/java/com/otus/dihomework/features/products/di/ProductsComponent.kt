package com.otus.dihomework.features.products.di

import com.otus.dihomework.common.di.FeatureScope
import com.otus.dihomework.features.products.ProductsViewModelFactory
import dagger.Component

@FeatureScope
@Component(dependencies = [ProductsDependencies::class], modules = [ProductsModule::class])
interface ProductsComponent {

    fun viewModelFactory(): ProductsViewModelFactory

    @Component.Factory
    interface Factory {
        fun create(dependencies: ProductsDependencies): ProductsComponent
    }
}