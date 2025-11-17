package ru.otus.marketsample.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import ru.otus.common.data.products.ProductApiService
import ru.otus.common.data.promo.PromoApiService
import javax.inject.Singleton

@Module
object DataModule {

    @Singleton
    @Provides
    fun provideProductApiService(
        retrofit: Retrofit
    ): ProductApiService {
        return retrofit.create(ProductApiService::class.java)
    }

    @Singleton
    @Provides
    fun providePromoApiService(
        retrofit: Retrofit
    ): PromoApiService {
        return retrofit.create(PromoApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideCoroutineDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }

    private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app")

    @Singleton
    @Provides
    fun provideDataStoreOfPreferences(
        applicationContext: Context
    ): DataStore<Preferences> {
        return applicationContext.appDataStore
    }
}