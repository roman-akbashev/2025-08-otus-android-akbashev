package com.linguacards.core.data.repository

import com.linguacards.core.database.dao.CardDao
import com.linguacards.core.database.entity.CardEntity
import com.linguacards.core.model.Card
import com.linguacards.core.network.api.DictionaryApiService
import com.linguacards.core.network.dto.DefinitionDto
import com.linguacards.core.network.dto.MeaningDto
import com.linguacards.core.network.dto.WordResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardRepositoryImplTest {

    private lateinit var cardDao: CardDao
    private lateinit var dictionaryApiService: DictionaryApiService
    private lateinit var repository: CardRepositoryImpl

    private val now = Clock.System.now()
    private val nowMillis = now.toEpochMilliseconds()
    private val deckId = 1L

    private val cardEntity1 = CardEntity(
        id = 1,
        deckId = deckId,
        word = "Hello",
        translation = "Привет",
        example = "Hello world",
        transcription = "həˈləʊ",
        easinessFactor = 2.5,
        interval = 5,
        repetitions = 2,
        nextReviewDate = nowMillis + 86400000, // завтра
        createdAt = nowMillis,
        updatedAt = nowMillis
    )

    private val cardEntity2 = CardEntity(
        id = 2,
        deckId = deckId,
        word = "Goodbye",
        translation = "Пока",
        example = null,
        transcription = null,
        easinessFactor = 2.5,
        interval = 3,
        repetitions = 1,
        nextReviewDate = null,
        createdAt = nowMillis,
        updatedAt = nowMillis
    )

    @Before
    fun setup() {
        cardDao = mockk()
        dictionaryApiService = mockk()
        repository = CardRepositoryImpl(cardDao, dictionaryApiService)
    }

    @Test
    fun `getCardsByDeckId should return list of Card`() = runTest {
        // Given
        coEvery { cardDao.getCardsByDeckId(deckId) } returns flowOf(
            listOf(
                cardEntity1,
                cardEntity2
            )
        )

        // When
        val result = repository.getCardsByDeckId(deckId).first()

        // Then
        assertEquals(2, result.size)
        val card1 = result.find { it.id == 1L }
        assertNotNull(card1)
        assertEquals("Hello", card1?.word)
        assertEquals("Привет", card1?.translation)
        assertEquals("Hello world", card1?.example)
        assertEquals("həˈləʊ", card1?.transcription)
        assertEquals(2.5, card1?.easinessFactor!!, 0.001)

        val card2 = result.find { it.id == 2L }
        assertNotNull(card2)
        assertEquals("Goodbye", card2?.word)
        assertNull(card2?.example)
        assertNull(card2?.transcription)
    }

    @Test
    fun `getCardById should return Card when exists`() = runTest {
        // Given
        coEvery { cardDao.getCardById(1) } returns cardEntity1

        // When
        val result = repository.getCardById(1)

        // Then
        assertNotNull(result)
        assertEquals(1L, result?.id)
        assertEquals("Hello", result?.word)
    }

    @Test
    fun `getCardById should return null when not exists`() = runTest {
        // Given
        coEvery { cardDao.getCardById(99) } returns null

        // When
        val result = repository.getCardById(99)

        // Then
        assertNull(result)
    }

    @Test
    fun `createCard should insert entity with createdAt and updatedAt`() = runTest {
        // Given
        val card = Card(
            deckId = deckId,
            word = "New",
            translation = "Новый",
            example = "Example",
            transcription = "njuː",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        val expectedId = 10L
        coEvery { cardDao.insertCard(any()) } returns expectedId

        // When
        val result = repository.createCard(card)

        // Then
        assertEquals(expectedId, result)
        coVerify {
            cardDao.insertCard(
                match {
                    it.deckId == deckId &&
                            it.word == "New" &&
                            it.translation == "Новый" &&
                            it.example == "Example" &&
                            it.transcription == "njuː" &&
                            it.createdAt > 0 &&
                            it.updatedAt > 0 &&
                            it.easinessFactor == 2.5 &&
                            it.interval == 0 &&
                            it.repetitions == 0 &&
                            it.nextReviewDate == null
                }
            )
        }
    }

    @Test
    fun `updateCard should update entity with new updatedAt`() = runTest {
        // Given
        val originalCard = Card(
            id = 1,
            deckId = deckId,
            word = "Hello",
            translation = "Привет",
            createdAt = now,
            updatedAt = now
        )
        val updatedCard = originalCard.copy(
            translation = "Приветик",
            updatedAt = Clock.System.now()
        )

        coEvery { cardDao.updateCard(any()) } returns Unit

        // When
        repository.updateCard(updatedCard)

        // Then
        coVerify {
            cardDao.updateCard(
                match {
                    it.id == updatedCard.id &&
                            it.word == updatedCard.word &&
                            it.translation == updatedCard.translation &&
                            it.updatedAt >= nowMillis
                }
            )
        }
    }

    @Test
    fun `deleteCard should delete entity when exists`() = runTest {
        // Given
        coEvery { cardDao.getCardById(1) } returns cardEntity1
        coEvery { cardDao.deleteCard(any()) } returns Unit

        // When
        repository.deleteCard(1)

        // Then
        coVerify { cardDao.getCardById(1) }
        coVerify { cardDao.deleteCard(cardEntity1) }
    }

    @Test
    fun `deleteCard should do nothing when card not exists`() = runTest {
        // Given
        coEvery { cardDao.getCardById(99) } returns null

        // When
        repository.deleteCard(99)

        // Then
        coVerify { cardDao.getCardById(99) }
        coVerify(exactly = 0) { cardDao.deleteCard(any()) }
    }

    @Test
    fun `deleteAllCards should call dao deleteAllCards`() = runTest {
        // Given
        coEvery { cardDao.deleteAllCards() } returns Unit

        // When
        repository.deleteAllCards()

        // Then
        coVerify { cardDao.deleteAllCards() }
    }

    @Test
    fun `getCardsForStudy should return cards with null or past nextReviewDate within limit`() =
        runTest {
            // Given
            val limit = 10
            val expectedEntities = listOf(cardEntity2)

            coEvery {
                cardDao.getCardsForStudy(
                    eq(deckId),
                    any(),
                    eq(limit)
                )
            } returns expectedEntities

            // When
            val result = repository.getCardsForStudy(deckId, limit)

            // Then
            assertEquals(1, result.size)
            assertEquals(2, result[0].id)
            coVerify { cardDao.getCardsForStudy(eq(deckId), any(), eq(limit)) }
        }

    @Test
    fun `updateCardAfterReview should call updateCard`() = runTest {
        // Given
        val card = Card(
            id = 1,
            deckId = deckId,
            word = "Hello",
            translation = "Привет",
            createdAt = now,
            updatedAt = now
        )
        coEvery { cardDao.updateCard(any()) } returns Unit

        // When
        repository.updateCardAfterReview(card)

        // Then
        coVerify { cardDao.updateCard(any()) }
    }

    @Test
    fun `fetchWordDetails should return success when API returns data`() = runTest {
        // Given
        val word = "test"
        val responseDto = WordResponseDto(
            word = "test",
            phonetic = "/test/",
            meanings = listOf(
                MeaningDto(
                    definitions = listOf(
                        DefinitionDto(
                            definition = "a test",
                            example = "This is a test"
                        )
                    )
                )
            )
        )
        coEvery { dictionaryApiService.getWordInfo(word) } returns listOf(responseDto)

        // When
        val result = repository.fetchWordDetails(word)

        // Then
        assertTrue(result.isSuccess)
        val details = result.getOrNull()
        assertNotNull(details)
        assertEquals("test", details?.word)
        assertEquals("test", details?.transcription) // without slashes
        assertEquals("This is a test", details?.example)
    }

    @Test
    fun `fetchWordDetails should return failure when API returns empty list`() = runTest {
        // Given
        val word = "nonexistent"
        coEvery { dictionaryApiService.getWordInfo(word) } returns emptyList()

        // When
        val result = repository.fetchWordDetails(word)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Word not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `fetchWordDetails should return failure when API throws exception`() = runTest {
        // Given
        val word = "error"
        coEvery { dictionaryApiService.getWordInfo(word) } throws RuntimeException("Network error")

        // When
        val result = repository.fetchWordDetails(word)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `fetchWordDetails should handle missing phonetic and example gracefully`() = runTest {
        // Given
        val word = "minimal"
        val responseDto = WordResponseDto(
            word = "minimal",
            phonetic = null,
            meanings = listOf(
                MeaningDto(
                    definitions = listOf(
                        DefinitionDto(
                            definition = "minimal definition",
                            example = null
                        )
                    )
                )
            )
        )
        coEvery { dictionaryApiService.getWordInfo(word) } returns listOf(responseDto)

        // When
        val result = repository.fetchWordDetails(word)

        // Then
        assertTrue(result.isSuccess)
        val details = result.getOrNull()
        assertNotNull(details)
        assertEquals("minimal", details?.word)
        assertNull(details?.transcription)
        assertNull(details?.example)
    }
}