package com.linguacards.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.linguacards.core.database.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY nextReviewDate ASC")
    fun getCardsByDeckId(deckId: Long): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :cardId")
    suspend fun getCardById(cardId: Long): CardEntity?

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND (nextReviewDate IS NULL OR nextReviewDate <= :currentTime) LIMIT :limit")
    suspend fun getCardsForStudy(deckId: Long, currentTime: Long, limit: Int): List<CardEntity>

    @Insert
    suspend fun insertCard(card: CardEntity): Long

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId")
    fun getCardCount(deckId: Long): Flow<Int>

    @Query("DELETE FROM cards")
    suspend fun deleteAllCards()
}