package com.otus.dihomework.di

import android.content.Context
import com.otus.dihomework.features.favorites.di.FavoritesComponent
import com.otus.dihomework.features.products.di.ProductsDependencies
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        NetworkModule::class,
        SubcomponentsModule::class,
        UseCaseModule::class,
        RepositoryModule::class
    ]
)
interface AppComponent : ProductsDependencies {
    fun favoritesComponent(): FavoritesComponent.Factory


    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}