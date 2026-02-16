package com.otus.dihomework.features.products.di

import com.otus.dihomework.common.di.FeatureScope
import com.otus.dihomework.common.util.PriceFormatter
import com.otus.dihomework.features.products.ProductsStateFactory
import dagger.Module
import dagger.Provides

@Module
object ProductsModule {

    @Provides
    @FeatureScope
    fun provideProductsStateFactory(priceFormatter: PriceFormatter): ProductsStateFactory {
        return ProductsStateFactory(priceFormatter)
    }
}