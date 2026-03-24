package com.linguacards.core.data.repository

import com.linguacards.core.database.dao.CardDao
import com.linguacards.core.database.dao.DeckDao
import com.linguacards.core.database.entity.DeckEntity
import com.linguacards.core.domain.repository.DeckRepository
import com.linguacards.core.model.Deck
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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
        // Комбинируем список колод с потоками количества карточек
        return combine(
            deckDao.getAllDecks(),
            getCardCountsFlow()
        ) { deckEntities, cardCounts ->
            deckEntities.map { entity ->
                entity.toDomain(cardCounts[entity.id] ?: 0)
            }
        }
    }

    override fun getDeckById(deckId: Long): Flow<Deck?> {
        // Используем Flow версию для реактивного обновления количества карточек
        return combine(
            deckDao.getAllDecks().map { decks -> decks.find { it.id == deckId } },
            cardDao.getCardCount(deckId).onStart { emit(0) }
        ) { deckEntity, cardCount ->
            deckEntity?.toDomain(cardCount)
        }
    }

    // Вспомогательный метод для получения Flow с количеством карточек для всех колод
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getCardCountsFlow(): Flow<Map<Long, Int>> {
        // Получаем список всех колод и для каждой подписываемся на изменения количества
        return deckDao.getAllDecks().map { decks ->
            decks.map { deck ->
                deck.id to cardDao.getCardCount(deck.id)
            }
        }.flatMapLatest { flows ->
            // Комбинируем все потоки в один
            combine(flows.map { (id, flow) ->
                flow.map { count -> id to count }
            }) { arrays ->
                arrays.toMap()
            }
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