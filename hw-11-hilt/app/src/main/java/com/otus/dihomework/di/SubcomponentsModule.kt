package com.otus.dihomework.di

import com.otus.dihomework.features.favorites.di.FavoritesComponent
import dagger.Module

@Module(subcomponents = [FavoritesComponent::class])
object SubcomponentsModule