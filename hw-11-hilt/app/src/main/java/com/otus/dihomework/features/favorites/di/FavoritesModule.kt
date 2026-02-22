package com.otus.dihomework.features.favorites.di

import com.otus.dihomework.common.di.FeatureScope
import com.otus.dihomework.common.util.PriceFormatter
import com.otus.dihomework.features.favorites.FavoritesStateFactory
import dagger.Module
import dagger.Provides

@Module
object FavoritesModule {

    @Provides
    @FeatureScope
    fun provideFavoritesStateFactory(priceFormatter: PriceFormatter): FavoritesStateFactory {
        return FavoritesStateFactory(priceFormatter)
    }
}