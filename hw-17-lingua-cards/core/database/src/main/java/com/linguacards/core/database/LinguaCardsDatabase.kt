package com.linguacards.core.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.linguacards.core.database.dao.CardDao
import com.linguacards.core.database.dao.DeckDao
import com.linguacards.core.database.entity.CardEntity
import com.linguacards.core.database.entity.DeckEntity
import com.linguacards.core.database.util.Converters

@Database(
    entities = [DeckEntity::class, CardEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LinguaCardsDatabase : RoomDatabase() {

    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao

    companion object {
        @Volatile
        private var INSTANCE: LinguaCardsDatabase? = null

        fun getInstance(context: Context): LinguaCardsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LinguaCardsDatabase::class.java,
                    "lingua_cards.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}