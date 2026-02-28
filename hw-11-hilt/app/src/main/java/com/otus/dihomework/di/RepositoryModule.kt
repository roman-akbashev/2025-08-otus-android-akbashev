package com.otus.dihomework.di

import com.otus.dihomework.common.data.FavoritesRepositoryImpl
import com.otus.dihomework.common.data.ProductRepositoryImpl
import com.otus.dihomework.common.domain_impl.FavoritesRepository
import com.otus.dihomework.common.domain_impl.ProductRepository
import dagger.Binds
import dagger.Module

@Module
interface RepositoryModule {

    @Binds
    fun favoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    fun productRepository(impl: ProductRepositoryImpl): ProductRepository
}