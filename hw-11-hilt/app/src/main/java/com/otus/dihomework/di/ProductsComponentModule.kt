package com.otus.dihomework.di

import com.otus.dihomework.features.products.di.DaggerProductsComponent
import com.otus.dihomework.features.products.di.ProductsComponent
import com.otus.dihomework.features.products.di.ProductsDependencies
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object ProductsComponentModule {

    @Provides
    @Singleton
    fun provideProductsComponent(
        appComponent: AppComponent
    ): ProductsComponent {
        return DaggerProductsComponent.factory()
            .create(appComponent as ProductsDependencies)
    }
}