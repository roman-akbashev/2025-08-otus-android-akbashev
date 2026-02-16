package com.otus.dihomework.features.products.di

import com.otus.dihomework.common.di.Dependencies
import com.otus.dihomework.common.domain_api.ConsumeProductsUseCase
import com.otus.dihomework.common.domain_api.ToggleFavoriteUseCase
import com.otus.dihomework.common.util.PriceFormatter

interface ProductsDependencies : Dependencies {
    fun provideConsumeProductsUseCase(): ConsumeProductsUseCase
    fun provideToggleFavoriteUseCase(): ToggleFavoriteUseCase
    fun providePriceFormatter(): PriceFormatter
}