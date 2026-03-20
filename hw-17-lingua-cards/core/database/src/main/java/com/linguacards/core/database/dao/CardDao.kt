package com.linguacards.core.database.dao

import androidx.room.*
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
    suspend fun getCardCount(deckId: Long): Int

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND nextReviewDate <= :currentTime")
    suspend fun getCardsDueCount(deckId: Long, currentTime: Long): Int

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND nextReviewDate IS NULL")
    suspend fun getNewCardsCount(deckId: Long): Int
}