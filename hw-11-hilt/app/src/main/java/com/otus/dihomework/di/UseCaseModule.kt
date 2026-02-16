package com.otus.dihomework.di

import com.otus.dihomework.common.domain_api.ConsumeFavoritesUseCase
import com.otus.dihomework.common.domain_api.ConsumeProductsUseCase
import com.otus.dihomework.common.domain_api.ToggleFavoriteUseCase
import com.otus.dihomework.common.domain_impl.ConsumeFavoritesUseCaseImpl
import com.otus.dihomework.common.domain_impl.ConsumeProductsUseCaseImpl
import com.otus.dihomework.common.domain_impl.FavoritesRepository
import com.otus.dihomework.common.domain_impl.ProductRepository
import com.otus.dihomework.common.domain_impl.ToggleFavoriteUseCaseImpl
import dagger.Module
import dagger.Provides

@Module
object UseCaseModule {

    @Provides
    fun provideConsumeProductsUseCase(
        productRepository: ProductRepository,
        favoritesRepository: FavoritesRepository
    ): ConsumeProductsUseCase {
        return ConsumeProductsUseCaseImpl(productRepository, favoritesRepository)
    }

    @Provides
    fun provideConsumeFavoritesUseCase(
        productRepository: ProductRepository,
        favoritesRepository: FavoritesRepository
    ): ConsumeFavoritesUseCase {
        return ConsumeFavoritesUseCaseImpl(productRepository, favoritesRepository)
    }

    @Provides
    fun provideToggleFavoriteUseCase(
        favoritesRepository: FavoritesRepository
    ): ToggleFavoriteUseCase {
        return ToggleFavoriteUseCaseImpl(favoritesRepository)
    }
}