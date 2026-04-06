package com.linguacards.core.data.repository

import com.linguacards.core.database.dao.CardDao
import com.linguacards.core.database.dao.DeckDao
import com.linguacards.core.database.entity.DeckEntity
import com.linguacards.core.model.Deck
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeckRepositoryImplTest {

    private lateinit var deckDao: DeckDao
    private lateinit var cardDao: CardDao
    private lateinit var repository: DeckRepositoryImpl

    private val now = Clock.System.now()
    private val nowMillis = now.toEpochMilliseconds()

    private val deckEntity1 = DeckEntity(
        id = 1,
        name = "Deck 1",
        description = "Description 1",
        createdAt = nowMillis,
        updatedAt = nowMillis
    )
    private val deckEntity2 = DeckEntity(
        id = 2,
        name = "Deck 2",
        description = null,
        createdAt = nowMillis,
        updatedAt = nowMillis
    )

    private val cardCounts = listOf(
        CardDao.DeckCardCount(deckId = 1, count = 5),
        CardDao.DeckCardCount(deckId = 2, count = 0)
    )

    @Before
    fun setup() {
        deckDao = mockk()
        cardDao = mockk()
        repository = DeckRepositoryImpl(deckDao, cardDao)
    }

    @Test
    fun `getAllDecks should return list of Deck with card counts`() = runTest {
        // Given
        coEvery { deckDao.getAllDecks() } returns flowOf(listOf(deckEntity1, deckEntity2))
        coEvery { cardDao.getCardCounts() } returns flowOf(cardCounts)

        // When
        val result = repository.getAllDecks().first()

        // Then
        assertEquals(2, result.size)
        val deck1 = result.find { it.id == 1L }
        assertNotNull(deck1)
        assertEquals("Deck 1", deck1?.name)
        assertEquals("Description 1", deck1?.description)
        assertEquals(5, deck1?.cardCount)

        val deck2 = result.find { it.id == 2L }
        assertNotNull(deck2)
        assertEquals("Deck 2", deck2?.name)
        assertNull(deck2?.description)
        assertEquals(0, deck2?.cardCount)
    }

    @Test
    fun `getDeckById should return Deck with card count when deck exists`() = runTest {
        // Given
        coEvery { deckDao.getDeckByIdFlow(1L) } returns flowOf(deckEntity1)
        coEvery { cardDao.getCardCount(1L) } returns flowOf(5)

        // When
        val result = repository.getDeckById(1L)
            .drop(1)  // пропускаем первое значение (0)
            .first()

        // Then
        assertNotNull(result)
        assertEquals(1L, result?.id)
        assertEquals("Deck 1", result?.name)
        assertEquals(5, result?.cardCount)
    }

    @Test
    fun `getDeckById should return null when deck does not exist`() = runTest {
        // Given
        coEvery { deckDao.getDeckByIdFlow(99) } returns flowOf(null)
        coEvery { cardDao.getCardCount(99) } returns flowOf(0)

        // When
        val result = repository.getDeckById(99).first()

        // Then
        assertNull(result)
    }

    @Test
    fun `createDeck should insert entity and return id`() = runTest {
        // Given
        val name = "New Deck"
        val description = "New Description"
        val expectedId = 10L
        coEvery { deckDao.insertDeck(any()) } returns expectedId

        // When
        val result = repository.createDeck(name, description)

        // Then
        assertEquals(expectedId, result)
        coVerify {
            deckDao.insertDeck(
                match {
                    it.name == name &&
                            it.description == description &&
                            it.createdAt > 0 &&
                            it.updatedAt > 0
                }
            )
        }
    }

    @Test
    fun `createDeck should handle null description`() = runTest {
        // Given
        val name = "New Deck"
        coEvery { deckDao.insertDeck(any()) } returns 5L

        // When
        val result = repository.createDeck(name, null)

        // Then
        assertEquals(5L, result)
        coVerify {
            deckDao.insertDeck(
                match {
                    it.name == name &&
                            it.description == null
                }
            )
        }
    }

    @Test
    fun `updateDeck should update entity with new updatedAt`() = runTest {
        // Given
        val originalDeck = Deck(
            id = 1,
            name = "Old Name",
            description = "Old Desc",
            createdAt = now,
            updatedAt = now,
            cardCount = 0
        )
        val updatedDeck = originalDeck.copy(
            name = "New Name",
            updatedAt = Clock.System.now()
        )

        coEvery { deckDao.updateDeck(any()) } returns Unit

        // When
        repository.updateDeck(updatedDeck)

        // Then
        coVerify {
            deckDao.updateDeck(
                match {
                    it.id == updatedDeck.id &&
                            it.name == updatedDeck.name &&
                            it.description == updatedDeck.description &&
                            it.updatedAt >= nowMillis
                }
            )
        }
    }

    @Test
    fun `deleteDeck should delete entity when deck exists`() = runTest {
        // Given
        coEvery { deckDao.getDeckById(1) } returns deckEntity1
        coEvery { deckDao.deleteDeck(any()) } returns Unit

        // When
        repository.deleteDeck(1)

        // Then
        coVerify { deckDao.getDeckById(1) }
        coVerify { deckDao.deleteDeck(deckEntity1) }
    }

    @Test
    fun `deleteDeck should do nothing when deck does not exist`() = runTest {
        // Given
        coEvery { deckDao.getDeckById(99) } returns null

        // When
        repository.deleteDeck(99)

        // Then
        coVerify { deckDao.getDeckById(99) }
        coVerify(exactly = 0) { deckDao.deleteDeck(any()) }
    }

    @Test
    fun `deleteAllDecks should call dao deleteAllDecks`() = runTest {
        // Given
        coEvery { deckDao.deleteAllDecks() } returns Unit

        // When
        repository.deleteAllDecks()

        // Then
        coVerify { deckDao.deleteAllDecks() }
    }
}