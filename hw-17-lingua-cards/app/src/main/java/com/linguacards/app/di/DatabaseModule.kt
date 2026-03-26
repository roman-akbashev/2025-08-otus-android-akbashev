package com.linguacards.app.di

import android.content.Context
import androidx.room.Room
import com.linguacards.core.database.LinguaCardsDatabase
import com.linguacards.core.database.dao.CardDao
import com.linguacards.core.database.dao.DeckDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LinguaCardsDatabase {
        return Room.databaseBuilder(
            context,
            LinguaCardsDatabase::class.java,
            "lingua_cards.db"
        )
//            .createFromAsset("databases/lingua_cards.db") // заполненная бд для демонмтрации
            .fallbackToDestructiveMigration(false) // Для разработки, в продакшене нужны миграции
            .build()
    }

    @Provides
    @Singleton
    fun provideDeckDao(database: LinguaCardsDatabase): DeckDao {
        return database.deckDao()
    }

    @Provides
    @Singleton
    fun provideCardDao(database: LinguaCardsDatabase): CardDao {
        return database.cardDao()
    }
}