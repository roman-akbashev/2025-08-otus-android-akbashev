package com.linguacards.core.data.repository

import com.linguacards.core.database.dao.DeckDao
import com.linguacards.core.database.entity.DeckEntity
import com.linguacards.core.domain.repository.DeckRepository
import com.linguacards.core.model.Deck
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstDeckRepository @Inject constructor(
    private val deckDao: DeckDao,
) : DeckRepository {

    override fun getAllDecks(): Flow<List<Deck>> {
        return deckDao.getAllDecks().map { entities ->
            entities.map { entity ->
                entity.toDomain()
            }
        }
    }

    override suspend fun getDeckById(deckId: Long): Deck? {
        return deckDao.getDeckById(deckId)?.toDomain()
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

    private fun DeckEntity.toDomain(): Deck {
        return Deck(
            id = id,
            name = name,
            description = description,
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            updatedAt = Instant.fromEpochMilliseconds(updatedAt),
            cardCount = 0 // Будет заполняться отдельно
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