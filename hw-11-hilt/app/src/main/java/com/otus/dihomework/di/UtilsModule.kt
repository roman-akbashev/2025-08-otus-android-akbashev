package com.otus.dihomework.di

import com.otus.dihomework.common.util.PriceFormatter
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object UtilsModule {

    @Provides
    @Singleton
    fun providePriceFormatter(): PriceFormatter {
        return PriceFormatter()
    }
}