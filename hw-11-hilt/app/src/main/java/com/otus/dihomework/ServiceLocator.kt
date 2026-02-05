package com.otus.dihomework

import android.content.Context
import com.google.gson.GsonBuilder
import com.otus.dihomework.common.data.FavoritesRepositoryImpl
import com.otus.dihomework.common.data.ProductApiService
import com.otus.dihomework.common.data.ProductDomainMapper
import com.otus.dihomework.common.data.ProductRemoteDataSource
import com.otus.dihomework.common.data.ProductRepositoryImpl
import com.otus.dihomework.common.domain_api.ConsumeFavoritesUseCase
import com.otus.dihomework.common.domain_api.ConsumeProductsUseCase
import com.otus.dihomework.common.domain_api.ToggleFavoriteUseCase
import com.otus.dihomework.common.domain_impl.ConsumeFavoritesUseCaseImpl
import com.otus.dihomework.common.domain_impl.ConsumeProductsUseCaseImpl
import com.otus.dihomework.common.domain_impl.FavoritesRepository
import com.otus.dihomework.common.domain_impl.ProductRepository
import com.otus.dihomework.common.domain_impl.ToggleFavoriteUseCaseImpl
import com.otus.dihomework.common.util.PriceFormatter
import com.otus.dihomework.features.favorites.FavoritesStateFactory
import com.otus.dihomework.features.products.ProductsStateFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceLocator {

    private var applicationContext: Context? = null

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    private val httpLoggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://otus-android.github.io/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
    }

    private val _productApiService: ProductApiService by lazy {
        retrofit.create(ProductApiService::class.java)
    }

    fun getProductApiService(): ProductApiService {
        return _productApiService
    }

    fun getProductDomainMapper(): ProductDomainMapper {
        return ProductDomainMapper()
    }

    fun getProductRemoteDataSource(): ProductRemoteDataSource {
        return ProductRemoteDataSource()
    }

    fun getFavoritesRepository(): FavoritesRepository {
        val context = applicationContext
        checkNotNull(context) { "ServiceLocator not initialized! Call init() first." }
        return FavoritesRepositoryImpl(context)
    }

    fun getProductRepository(): ProductRepository {
        return ProductRepositoryImpl()
    }

    fun getConsumeProductsUseCase(): ConsumeProductsUseCase {
        return ConsumeProductsUseCaseImpl()
    }

    fun getConsumeFavoritesUseCase(): ConsumeFavoritesUseCase {
        return ConsumeFavoritesUseCaseImpl()
    }

    fun getToggleFavoriteUseCase(): ToggleFavoriteUseCase {
        return ToggleFavoriteUseCaseImpl()
    }

    fun getPriceFormatter(): PriceFormatter {
        return PriceFormatter()
    }

    fun getProductsStateFactory(): ProductsStateFactory {
        return ProductsStateFactory()
    }

    fun getFavoritesStateFactory(): FavoritesStateFactory {
        return FavoritesStateFactory()
    }
}
