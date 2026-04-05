package com.linguacards.core.data.repository

import com.linguacards.core.database.dao.CardDao
import com.linguacards.core.database.dao.DeckDao
import com.linguacards.core.database.entity.DeckEntity
import com.linguacards.core.domain.repository.DeckRepository
import com.linguacards.core.model.Deck
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeckRepositoryImpl @Inject constructor(
    private val deckDao: DeckDao,
    private val cardDao: CardDao,
) : DeckRepository {

    override fun getAllDecks(): Flow<List<Deck>> {
        return combine(
            deckDao.getAllDecks(),
            cardDao.getCardCounts()
        ) { deckEntities, cardCountsList ->
            val cardCountsMap = cardCountsList.associate { it.deckId to it.count }
            deckEntities.map { entity ->
                entity.toDomain(cardCountsMap[entity.id] ?: 0)
            }
        }
    }

    override fun getDeckById(deckId: Long): Flow<Deck?> {
        return combine(
            deckDao.getDeckByIdFlow(deckId),
            cardDao.getCardCount(deckId).onStart { emit(0) }
        ) { deckEntity, cardCount ->
            deckEntity?.toDomain(cardCount)
        }
    }

    override suspend fun createDeck(name: String, description: String?): Long {
        val now = Clock.System.now().toEpochMilliseconds()
        val entity = DeckEntity(
            name = name,
            description = description,
            createdAt = now,
            updatedAt = now
        )
        return deckDao.insertDeck(entity)
    }

    override suspend fun updateDeck(deck: Deck) {
        val entity = deck.toEntity()
        deckDao.updateDeck(entity)
    }

    override suspend fun deleteDeck(deckId: Long) {
        val deck = deckDao.getDeckById(deckId) ?: return
        deckDao.deleteDeck(deck)
    }

    override suspend fun deleteAllDecks() {
        deckDao.deleteAllDecks()
    }


    private fun DeckEntity.toDomain(cardCount: Int): Deck {
        return Deck(
            id = id,
            name = name,
            description = description,
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            updatedAt = Instant.fromEpochMilliseconds(updatedAt),
            cardCount = cardCount
        )
    }

    private fun Deck.toEntity(): DeckEntity {
        return DeckEntity(
            id = id,
            name = name,
            description = description,
            createdAt = createdAt.toEpochMilliseconds(),
            updatedAt = updatedAt.toEpochMilliseconds()
        )
    }
}