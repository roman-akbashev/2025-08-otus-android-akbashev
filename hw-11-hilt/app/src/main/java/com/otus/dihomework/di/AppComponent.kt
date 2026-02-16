package com.otus.dihomework.di

import android.content.Context
import com.otus.dihomework.common.di.Dependencies
import com.otus.dihomework.common.domain_api.ConsumeProductsUseCase
import com.otus.dihomework.common.domain_api.ToggleFavoriteUseCase
import com.otus.dihomework.common.util.PriceFormatter
import com.otus.dihomework.features.favorites.di.FavoritesComponent
import com.otus.dihomework.features.products.di.ProductsComponent
import com.otus.dihomework.features.products.di.ProductsDependencies
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        NetworkModule::class,
        RepositoryModule::class,
        UseCaseModule::class,
        UtilsModule::class,
        SubcomponentsModule::class,
        ProductsComponentModule::class
    ]
)
interface AppComponent : ProductsDependencies, Dependencies {

    override fun provideConsumeProductsUseCase(): ConsumeProductsUseCase
    override fun provideToggleFavoriteUseCase(): ToggleFavoriteUseCase
    override fun providePriceFormatter(): PriceFormatter

    fun favoritesComponent(): FavoritesComponent.Factory

    fun productsComponent(): ProductsComponent

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}