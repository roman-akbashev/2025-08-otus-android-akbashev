package com.linguacards.core.data.repository

import com.linguacards.core.database.dao.CardDao
import com.linguacards.core.database.entity.CardEntity
import com.linguacards.core.domain.repository.CardRepository
import com.linguacards.core.model.Card
import com.linguacards.core.model.WordDetails
import com.linguacards.core.network.api.DictionaryApiService
import com.linguacards.core.network.mapper.WordDetailsMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepositoryImpl @Inject constructor(
    private val cardDao: CardDao,
    private val dictionaryApiService: DictionaryApiService,
) : CardRepository {

    override fun getCardsByDeckId(deckId: Long): Flow<List<Card>> {
        return cardDao.getCardsByDeckId(deckId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCardById(cardId: Long): Card? {
        return cardDao.getCardById(cardId)?.toDomain()
    }

    override suspend fun createCard(card: Card): Long {
        val now = Clock.System.now().toEpochMilliseconds()
        val entity = card.copy(
            createdAt = Instant.fromEpochMilliseconds(now),
            updatedAt = Instant.fromEpochMilliseconds(now)
        ).toEntity()
        return cardDao.insertCard(entity)
    }

    override suspend fun updateCard(card: Card) {
        val now = Clock.System.now().toEpochMilliseconds()
        val entity = card.copy(
            updatedAt = Instant.fromEpochMilliseconds(now)
        ).toEntity()
        cardDao.updateCard(entity)
    }

    override suspend fun deleteCard(cardId: Long) {
        val card = cardDao.getCardById(cardId) ?: return
        cardDao.deleteCard(card)
    }

    override suspend fun getCardsForStudy(deckId: Long, limit: Int): List<Card> {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        return cardDao.getCardsForStudy(deckId, currentTime, limit)
            .map { it.toDomain() }
    }

    override suspend fun updateCardAfterReview(card: Card) {
        updateCard(card)
    }

    override suspend fun fetchWordDetails(word: String): Result<WordDetails> {
        return try {
            val response = dictionaryApiService.getWordInfo(word)
            if (response.isNotEmpty()) {
                val details = WordDetailsMapper.mapToDomain(response.first())
                Result.success(details)
            } else {
                Result.failure(Exception("Word not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun CardEntity.toDomain(): Card {
        return Card(
            id = id,
            deckId = deckId,
            word = word,
            translation = translation,
            example = example,
            transcription = transcription,
            easinessFactor = easinessFactor,
            interval = interval,
            repetitions = repetitions,
            nextReviewDate = nextReviewDate?.let { Instant.fromEpochMilliseconds(it) },
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            updatedAt = Instant.fromEpochMilliseconds(updatedAt)
        )
    }

    private fun Card.toEntity(): CardEntity {
        return CardEntity(
            id = id,
            deckId = deckId,
            word = word,
            translation = translation,
            example = example,
            transcription = transcription,
            easinessFactor = easinessFactor,
            interval = interval,
            repetitions = repetitions,
            nextReviewDate = nextReviewDate?.toEpochMilliseconds(),
            createdAt = createdAt.toEpochMilliseconds(),
            updatedAt = updatedAt.toEpochMilliseconds()
        )
    }
}