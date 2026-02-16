package com.otus.dihomework.features.favorites.di

import com.otus.dihomework.common.di.FeatureScope
import com.otus.dihomework.features.favorites.FavoritesViewModelFactory
import dagger.Subcomponent

@FeatureScope
@Subcomponent(modules = [FavoritesModule::class])
interface FavoritesComponent {

    fun viewModelFactory(): FavoritesViewModelFactory

    @Subcomponent.Factory
    interface Factory {
        fun create(): FavoritesComponent
    }
}