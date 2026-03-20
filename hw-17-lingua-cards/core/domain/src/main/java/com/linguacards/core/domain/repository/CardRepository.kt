package com.linguacards.core.domain.repository

import com.linguacards.core.model.Card
import com.linguacards.core.model.WordDetails
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    fun getCardsByDeckId(deckId: Long): Flow<List<Card>>
    suspend fun getCardById(cardId: Long): Card?
    suspend fun createCard(card: Card): Long
    suspend fun updateCard(card: Card)
    suspend fun deleteCard(cardId: Long)
    suspend fun getCardsForStudy(deckId: Long, limit: Int = 20): List<Card>
    suspend fun updateCardAfterReview(card: Card)
    suspend fun fetchWordDetails(word: String): Result<WordDetails>
}