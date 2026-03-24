package com.linguacards.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.linguacards.core.database.dao.CardDao
import com.linguacards.core.database.dao.DeckDao
import com.linguacards.core.database.entity.CardEntity
import com.linguacards.core.database.entity.DeckEntity

@Database(
    entities = [DeckEntity::class, CardEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LinguaCardsDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao
}