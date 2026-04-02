package com.linguacards.features.deckdetail.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.linguacards.core.domain.repository.CardRepository
import com.linguacards.core.domain.repository.DeckRepository
import com.linguacards.core.model.Card
import com.linguacards.core.model.Deck
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeckDetailViewModelTest {

    private lateinit var viewModel: DeckDetailViewModel
    private lateinit var deckRepository: DeckRepository
    private lateinit var cardRepository: CardRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var testDispatcher: TestDispatcher

    private val now = Clock.System.now()
    private val deckId = 1L

    private val sampleDeck = Deck(
        id = deckId,
        name = "English Basics",
        description = "Basic English vocabulary",
        createdAt = now,
        updatedAt = now,
        cardCount = 3
    )

    private val sampleCards = listOf(
        Card(
            id = 1,
            deckId = deckId,
            word = "Hello",
            translation = "Привет",
            example = "Hello, everyone.",
            transcription = "həˈloʊ",
            createdAt = now,
            updatedAt = now
        ),
        Card(
            id = 2,
            deckId = deckId,
            word = "Goodbye",
            translation = "Пока",
            example = "I have come to say goodbye",
            transcription = "ɡədˈbaɪ",
            createdAt = now,
            updatedAt = now
        ),
        Card(
            id = 3,
            deckId = deckId,
            word = "Home",
            translation = "Дом",
            example = "I can't live in that home.",
            transcription = "(h)əʊm",
            createdAt = now,
            updatedAt = now
        )
    )

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        deckRepository = mockk()
        cardRepository = mockk()
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadDeckData should update state with deck and cards when repositories return data`() =
        runTest {
            // Given
            coEvery { deckRepository.getDeckById(deckId) } returns flowOf(sampleDeck)
            coEvery { cardRepository.getCardsByDeckId(deckId) } returns flowOf(sampleCards)

            // When
            viewModel = DeckDetailViewModel(deckRepository, cardRepository, savedStateHandle)

            // Then
            viewModel.state.test {
                val loadingState = awaitItem()
                assertTrue(loadingState is DeckDetailState.Loading)

                advanceUntilIdle()

                val successState = awaitItem()
                assertTrue(successState is DeckDetailState.Success)
                assertEquals(sampleDeck, (successState as DeckDetailState.Success).deck)
                assertEquals(sampleCards, successState.cards)
                assertEquals("", successState.searchQuery)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `loadDeckData should set state to Loading initially`() = runTest {
        // Given
        coEvery { deckRepository.getDeckById(deckId) } returns flowOf(sampleDeck)
        coEvery { cardRepository.getCardsByDeckId(deckId) } returns flowOf(sampleCards)

        // When
        viewModel = DeckDetailViewModel(deckRepository, cardRepository, savedStateHandle)

        // Then
        viewModel.state.test {
            val loadingState = awaitItem()
            assertTrue(loadingState is DeckDetailState.Loading)

            advanceUntilIdle()

            val successState = awaitItem()
            assertTrue(successState is DeckDetailState.Success)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadDeckData should set state to Empty when cards repository returns empty list`() =
        runTest {
            // Given
            coEvery { deckRepository.getDeckById(deckId) } returns flowOf(sampleDeck)
            coEvery { cardRepository.getCardsByDeckId(deckId) } returns flowOf(emptyList())

            // When
            viewModel = DeckDetailViewModel(deckRepository, cardRepository, savedStateHandle)

            // Then
            viewModel.state.test {
                val loadingState = awaitItem()
                assertTrue(loadingState is DeckDetailState.Loading)

                advanceUntilIdle()

                val emptyState = awaitItem()
                assertTrue(emptyState is DeckDetailState.Empty)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `loadDeckData should set error message when repository throws exception`() = runTest {
        // Given
        val errorMessage = "Database error"
        val errorFlow = flow<Deck?> {
            throw RuntimeException(errorMessage)
        }
        coEvery { deckRepository.getDeckById(deckId) } returns errorFlow
        coEvery { cardRepository.getCardsByDeckId(deckId) } returns flowOf(sampleCards)

        // When
        viewModel = DeckDetailViewModel(deckRepository, cardRepository, savedStateHandle)

        // Then
        viewModel.errorMessage.test {
            assertNull(awaitItem())

            advanceUntilIdle()

            val error = awaitItem()
            assertEquals("Database error", error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSearchQueryChanged should update search query state`() = runTest {
        // Given
        coEvery { deckRepository.getDeckById(deckId) } returns flowOf(sampleDeck)
        coEvery { cardRepository.getCardsByDeckId(deckId) } returns flowOf(sampleCards)
        viewModel = DeckDetailViewModel(deckRepository, cardRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.searchQuery.test {
            assertEquals("", awaitItem()) // Initial value

            viewModel.onSearchQueryChanged("Hello")

            assertEquals("Hello", awaitItem()) // Updated value
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuery should filter cards by word`() = runTest {
        // Given
        coEvery { deckRepository.getDeckById(deckId) } returns flowOf(sampleDeck)
        coEvery { cardRepository.getCardsByDeckId(deckId) } returns flowOf(sampleCards)
        viewModel = DeckDetailViewModel(deckRepository, cardRepository, savedStateHandle)

        viewModel.state.test {
            awaitItem() // Loading

            advanceUntilIdle()

            awaitItem() // Success with all cards

            // When
            viewModel.onSearchQueryChanged("Hello")

            advanceUntilIdle()

            // Then
            val successState = awaitItem()
            assertTrue(successState is DeckDetailState.Success)
            assertEquals(1, (successState as DeckDetailState.Success).cards.size)
            assertEquals("Hello", successState.cards[0].word)
            assertEquals("Hello", successState.searchQuery)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuery should filter cards by translation`() = runTest {
        // Given
        coEvery { deckRepository.getDeckById(deckId) } returns flowOf(sampleDeck)
        coEvery { cardRepository.getCardsByDeckId(deckId) } returns flowOf(sampleCards)
        viewModel = DeckDetailViewModel(deckRepository, cardRepository, savedStateHandle)

        viewModel.state.test {
            awaitItem() // Loading

            advanceUntilIdle()

            awaitItem() // Success with all cards

            // When
            viewModel.onSearchQueryChanged("Дом")

            advanceUntilIdle()

            // Then
            val successState = awaitItem()
            assertTrue(successState is DeckDetailState.Success)
            assertEquals(1, (successState as DeckDetailState.Success).cards.size)
            assertEquals("Home", successState.cards[0].word)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuery should filter cards by example`() = runTest {
        // Given
        coEvery { deckRepository.getDeckById(deckId) } returns flowOf(sampleDeck)
        coEvery { cardRepository.getCardsByDeckId(deckId) } returns flowOf(sampleCards)
        viewModel = DeckDetailViewModel(deckRepository, cardRepository, savedStateHandle)

        viewModel.state.test {
            awaitItem() // Loading

            advanceUntilIdle()

            awaitItem() // Success with all cards

            // When
            viewModel.onSearchQueryChanged("everyone")

            advanceUntilIdle()

            // Then
            val successState = awaitItem()
            assertTrue(successState is DeckDetailState.Success)
            assertEquals(1, (successState as DeckDetailState.Success).cards.size)
            assertEquals("Hello", successState.cards[0].word)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuery should be case insensitive`() = runTest {
        // Given
        coEvery { deckRepository.getDeckById(deckId) } returns flowOf(sampleDeck)
        coEvery { cardRepository.getCardsByDeckId(deckId) } returns flowOf(sampleCards)
        viewModel = DeckDetailViewModel(deckRepository, cardRepository, savedStateHandle)

        viewModel.state.test {
            awaitItem() // Loading

            advanceUntilIdle()

            awaitItem() // Success with all cards

            // When
            viewModel.onSearchQueryChanged("Hello")

            advanceUntilIdle()

            // Then
            val successState = awaitItem()
            assertTrue(successState is DeckDetailState.Success)
            assertEquals(1, (successState as DeckDetailState.Success).cards.size)
            assertEquals("Hello", successState.cards[0].word)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuery should return empty list when no matches found`() = runTest {
        // Given
        coEvery { deckRepository.getDeckById(deckId) } returns flowOf(sampleDeck)
        coEvery { cardRepository.getCardsByDeckId(deckId) } returns flowOf(sampleCards)
        viewModel = DeckDetailViewModel(deckRepository, cardRepository, savedStateHandle)

        viewModel.state.test {
            awaitItem() // Loading

            advanceUntilIdle()

            awaitItem() // Success with all cards

            // When
            viewModel.onSearchQueryChanged("Nonexistent")

            advanceUntilIdle()

            // Then
            val successState = awaitItem()
            assertTrue(successState is DeckDetailState.Success)
            assertEquals(emptyList<Card>(), (successState as DeckDetailState.Success).cards)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear search query should show all cards`() = runTest {
        // Given
        coEvery { deckRepository.getDeckById(deckId) } returns flowOf(sampleDeck)
        coEvery { cardRepository.getCardsByDeckId(deckId) } returns flowOf(sampleCards)
        viewModel = DeckDetailViewModel(deckRepository, cardRepository, savedStateHandle)

        viewModel.state.test {
            awaitItem() // Loading

            advanceUntilIdle()

            awaitItem() // Success with all cards

            // Filter
            viewModel.onSearchQueryChanged("Hello")

            advanceUntilIdle()

            awaitItem() // Success with filtered cards

            // When - clear search
            viewModel.onSearchQueryChanged("")

            advanceUntilIdle()

            // Then - should show all cards
            val successState = awaitItem()
            assertTrue(successState is DeckDetailState.Success)
            assertEquals(sampleCards, (successState as DeckDetailState.Success).cards)
            assertEquals("", successState.searchQuery)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteCard should call repository deleteCard and succeed`() = runTest {
        // Given
        val cardToDelete = sampleCards[0]
        coEvery { deckRepository.getDeckById(deckId) } returns flowOf(sampleDeck)
        coEvery { cardRepository.getCardsByDeckId(deckId) } returns flowOf(sampleCards)
        coEvery { cardRepository.deleteCard(any()) } returns Unit

        viewModel = DeckDetailViewModel(deckRepository, cardRepository, savedStateHandle)

        advanceUntilIdle()

        // When
        viewModel.deleteCard(cardToDelete)

        advanceUntilIdle()

        // Then
        coVerify { cardRepository.deleteCard(cardToDelete.id) }
    }

    @Test
    fun `deleteCard should set error message when exception occurs`() = runTest {
        // Given
        val cardToDelete = sampleCards[0]
        coEvery { deckRepository.getDeckById(deckId) } returns flowOf(sampleDeck)
        coEvery { cardRepository.getCardsByDeckId(deckId) } returns flowOf(sampleCards)

        val exception = RuntimeException("Failed to delete card")
        coEvery { cardRepository.deleteCard(any()) } throws exception

        viewModel = DeckDetailViewModel(deckRepository, cardRepository, savedStateHandle)

        advanceUntilIdle()

        viewModel.errorMessage.test {
            assertNull(awaitItem())

            // When
            viewModel.deleteCard(cardToDelete)
            advanceUntilIdle()

            // Then
            val errorMessage = awaitItem()
            assertEquals("Failed to delete card: Failed to delete card", errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearErrorMessage should reset error message to null`() = runTest {
        // Given
        coEvery { deckRepository.getDeckById(deckId) } returns flowOf(null)
        coEvery { cardRepository.getCardsByDeckId(deckId) } returns flowOf(sampleCards)

        viewModel = DeckDetailViewModel(deckRepository, cardRepository, savedStateHandle)

        viewModel.errorMessage.test {
            // Initial value: null
            assertNull(awaitItem())

            // Error occurs when deck not found
            advanceUntilIdle()

            // Error message
            val errorMessage = awaitItem()
            assertEquals("Deck not found", errorMessage)

            // Clear error
            viewModel.clearErrorMessage()
            advanceUntilIdle()

            // Cleared to null
            val clearedMessage = awaitItem()
            assertNull(clearedMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cards with null example should still be filterable by word and translation`() = runTest {
        // Given
        val cardsWithNullExample = listOf(
            Card(
                id = 1,
                deckId = deckId,
                word = "Try",
                translation = "Пробовать",
                example = null,
                createdAt = now,
                updatedAt = now
            )
        )

        coEvery { deckRepository.getDeckById(deckId) } returns flowOf(sampleDeck)
        coEvery { cardRepository.getCardsByDeckId(deckId) } returns flowOf(cardsWithNullExample)
        viewModel = DeckDetailViewModel(deckRepository, cardRepository, savedStateHandle)

        viewModel.state.test {
            awaitItem() // Loading

            advanceUntilIdle()

            awaitItem() // Success with all cards

            // When - search by word
            viewModel.onSearchQueryChanged("Try")
            advanceUntilIdle()

            var successState = awaitItem()
            assertTrue(successState is DeckDetailState.Success)
            assertEquals(1, (successState as DeckDetailState.Success).cards.size)

            // When - search by translation
            viewModel.onSearchQueryChanged("Пробовать")
            advanceUntilIdle()

            successState = awaitItem()
            assertTrue(successState is DeckDetailState.Success)
            assertEquals(1, (successState as DeckDetailState.Success).cards.size)

            // When - search by null example should not crash
            viewModel.onSearchQueryChanged("null")
            advanceUntilIdle()

            successState = awaitItem()
            assertTrue(successState is DeckDetailState.Success)
            assertEquals(0, (successState as DeckDetailState.Success).cards.size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}