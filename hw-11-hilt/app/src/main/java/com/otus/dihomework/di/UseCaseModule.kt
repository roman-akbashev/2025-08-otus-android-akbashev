package com.otus.dihomework.di

import com.otus.dihomework.common.domain_api.ConsumeFavoritesUseCase
import com.otus.dihomework.common.domain_api.ConsumeProductsUseCase
import com.otus.dihomework.common.domain_api.ToggleFavoriteUseCase
import com.otus.dihomework.common.domain_impl.ConsumeFavoritesUseCaseImpl
import com.otus.dihomework.common.domain_impl.ConsumeProductsUseCaseImpl
import com.otus.dihomework.common.domain_impl.ToggleFavoriteUseCaseImpl
import dagger.Binds
import dagger.Module

@Module
interface UseCaseModule {

    @Binds
    fun consumeProductsUseCase(impl: ConsumeProductsUseCaseImpl): ConsumeProductsUseCase

    @Binds
    fun consumeFavoritesUseCase(impl: ConsumeFavoritesUseCaseImpl): ConsumeFavoritesUseCase

    @Binds
    fun toggleFavoriteUseCase(impl: ToggleFavoriteUseCaseImpl): ToggleFavoriteUseCase


}