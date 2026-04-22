package com.linguacards.app.di

import com.linguacards.core.data.repository.CardRepositoryImpl
import com.linguacards.core.data.repository.DeckRepositoryImpl
import com.linguacards.core.domain.repository.CardRepository
import com.linguacards.core.domain.repository.DeckRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDeckRepository(
        deckRepositoryImpl: DeckRepositoryImpl
    ): DeckRepository

    @Binds
    @Singleton
    abstract fun bindCardRepository(
        cardRepositoryImpl: CardRepositoryImpl
    ): CardRepository
}