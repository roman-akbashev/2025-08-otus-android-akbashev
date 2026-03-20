package com.linguacards.core.domain.repository

import com.linguacards.core.model.Deck
import kotlinx.coroutines.flow.Flow

interface DeckRepository {
    fun getAllDecks(): Flow<List<Deck>>
    suspend fun getDeckById(deckId: Long): Deck?
    suspend fun createDeck(name: String, description: String?): Long
    suspend fun updateDeck(deck: Deck)
    suspend fun deleteDeck(deckId: Long)
}