package com.otus.dihomework.di

import android.content.Context
import com.otus.dihomework.common.data.FavoritesRepositoryImpl
import com.otus.dihomework.common.data.ProductApiService
import com.otus.dihomework.common.data.ProductDomainMapper
import com.otus.dihomework.common.data.ProductRemoteDataSource
import com.otus.dihomework.common.data.ProductRepositoryImpl
import com.otus.dihomework.common.domain_impl.FavoritesRepository
import com.otus.dihomework.common.domain_impl.ProductRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProductDomainMapper(): ProductDomainMapper {
        return ProductDomainMapper()
    }

    @Provides
    @Singleton
    fun provideProductRemoteDataSource(
        apiService: ProductApiService
    ): ProductRemoteDataSource {
        return ProductRemoteDataSource(apiService)
    }

    @Provides
    @Singleton
    fun provideProductRepository(
        remoteDataSource: ProductRemoteDataSource,
        mapper: ProductDomainMapper
    ): ProductRepository {
        return ProductRepositoryImpl(remoteDataSource, mapper)
    }

    @Provides
    @Singleton
    fun provideFavoritesRepository(context: Context): FavoritesRepository {
        return FavoritesRepositoryImpl(context)
    }
}