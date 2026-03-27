package com.linguacards.features.decklist.presentation

import app.cash.turbine.test
import com.linguacards.core.domain.repository.DeckRepository
import com.linguacards.core.model.Deck
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
class DecksViewModelTest {

    private lateinit var viewModel: DecksViewModel
    private lateinit var deckRepository: DeckRepository
    private lateinit var testDispatcher: TestDispatcher

    private val now = Clock.System.now()

    private val sampleDecks = listOf(
        Deck(
            id = 1,
            name = "Spanish Basics",
            description = "Basic Spanish vocabulary",
            createdAt = now,
            updatedAt = now,
            cardCount = 10
        ),
        Deck(
            id = 2,
            name = "French Phrases",
            description = "Common French phrases",
            createdAt = now,
            updatedAt = now,
            cardCount = 15
        ),
        Deck(
            id = 3,
            name = "German Grammar",
            description = "German grammar rules",
            createdAt = now,
            updatedAt = now,
            cardCount = 8
        )
    )

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        deckRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load decks and set state to Loading initially`() = runTest {
        // Given
        coEvery { deckRepository.getAllDecks() } returns flowOf(emptyList())

        // When
        viewModel = DecksViewModel(deckRepository)

        // Then
        viewModel.state.test {
            val loadingState = awaitItem()
            assertTrue(loadingState is DecksState.Loading)

            // Process the flow
            advanceUntilIdle()

            val emptyState = awaitItem()
            assertTrue(emptyState is DecksState.Empty)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadDecks should update state with decks when repository returns data`() = runTest {
        // Given
        coEvery { deckRepository.getAllDecks() } returns flowOf(sampleDecks)

        // When
        viewModel = DecksViewModel(deckRepository)

        // Then
        viewModel.state.test {
            val loadingState = awaitItem()
            assertTrue(loadingState is DecksState.Loading)

            // Process the flow
            advanceUntilIdle()

            val successState = awaitItem()
            assertTrue(successState is DecksState.Success)
            assertEquals(sampleDecks, (successState as DecksState.Success).decks)
            assertEquals("", successState.searchQuery)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadDecks should set state to Empty when repository returns empty list`() = runTest {
        // Given
        coEvery { deckRepository.getAllDecks() } returns flowOf(emptyList())

        // When
        viewModel = DecksViewModel(deckRepository)

        // Then
        viewModel.state.test {
            val loadingState = awaitItem()
            assertTrue(loadingState is DecksState.Loading)

            // Process the flow
            advanceUntilIdle()

            val emptyState = awaitItem()
            assertTrue(emptyState is DecksState.Empty)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadDecks should set error message when repository throws exception`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        coEvery { deckRepository.getAllDecks() } answers {
            flow {
                delay(1)
                throw exception
            }
        }

        // When
        viewModel = DecksViewModel(deckRepository)

        // Then
        advanceUntilIdle()

        // Проверяем errorMessage
        val errorMessages = mutableListOf<String?>()
        val errorJob = launch {
            viewModel.errorMessage.collect { errorMessages.add(it) }
        }

        delay(100)
        advanceUntilIdle()

        println("Error messages collected: $errorMessages")
        assertTrue(errorMessages.any { it == "Database error" })

        errorJob.cancel()
    }

    @Test
    fun `onSearchQueryChanged should update search query state`() = runTest {
        // Given
        coEvery { deckRepository.getAllDecks() } returns flowOf(sampleDecks)
        viewModel = DecksViewModel(deckRepository)
        advanceUntilIdle()

        viewModel.searchQuery.test {
            assertEquals("", awaitItem()) // Initial value

            viewModel.onSearchQueryChanged("Spanish")

            assertEquals("Spanish", awaitItem()) // Updated value
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuery should filter decks by name`() = runTest {
        // Given
        coEvery { deckRepository.getAllDecks() } returns flowOf(sampleDecks)
        viewModel = DecksViewModel(deckRepository)

        viewModel.state.test {
            awaitItem() // Loading

            advanceUntilIdle()

            awaitItem() // Success with all decks

            // When
            viewModel.onSearchQueryChanged("Spanish")

            advanceUntilIdle()

            // Then
            val successState = awaitItem()
            assertTrue(successState is DecksState.Success)
            assertEquals(1, (successState as DecksState.Success).decks.size)
            assertEquals("Spanish Basics", successState.decks[0].name)
            assertEquals("Spanish", successState.searchQuery)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuery should filter decks by description`() = runTest {
        // Given
        coEvery { deckRepository.getAllDecks() } returns flowOf(sampleDecks)
        viewModel = DecksViewModel(deckRepository)

        viewModel.state.test {
            awaitItem() // Loading

            advanceUntilIdle()

            awaitItem() // Success with all decks

            // When
            viewModel.onSearchQueryChanged("French")

            advanceUntilIdle()

            // Then
            val successState = awaitItem()
            assertTrue(successState is DecksState.Success)
            assertEquals(1, (successState as DecksState.Success).decks.size)
            assertEquals("French Phrases", successState.decks[0].name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuery should be case insensitive`() = runTest {
        // Given
        coEvery { deckRepository.getAllDecks() } returns flowOf(sampleDecks)
        viewModel = DecksViewModel(deckRepository)

        viewModel.state.test {
            awaitItem() // Loading

            advanceUntilIdle()

            awaitItem() // Success with all decks

            // When
            viewModel.onSearchQueryChanged("spanish")

            advanceUntilIdle()

            // Then
            val successState = awaitItem()
            assertTrue(successState is DecksState.Success)
            assertEquals(1, (successState as DecksState.Success).decks.size)
            assertEquals("Spanish Basics", successState.decks[0].name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuery should return empty list when no matches found`() = runTest {
        // Given
        coEvery { deckRepository.getAllDecks() } returns flowOf(sampleDecks)
        viewModel = DecksViewModel(deckRepository)

        viewModel.state.test {
            awaitItem() // Loading

            advanceUntilIdle()

            awaitItem() // Success with all decks

            // When
            viewModel.onSearchQueryChanged("Nonexistent")

            advanceUntilIdle()

            // Then
            val successState = awaitItem()
            assertTrue(successState is DecksState.Success)
            assertEquals(emptyList<Deck>(), (successState as DecksState.Success).decks)
            assertEquals("Nonexistent", successState.searchQuery)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear search query should show all decks`() = runTest {
        // Given
        coEvery { deckRepository.getAllDecks() } returns flowOf(sampleDecks)
        viewModel = DecksViewModel(deckRepository)

        viewModel.state.test {
            awaitItem() // Loading

            advanceUntilIdle()

            awaitItem() // Success with all decks

            // Filter
            viewModel.onSearchQueryChanged("Spanish")

            advanceUntilIdle()

            awaitItem() // Success with filtered decks

            // When - clear search
            viewModel.onSearchQueryChanged("")

            advanceUntilIdle()

            // Then - should show all decks
            val successState = awaitItem()
            assertTrue(successState is DecksState.Success)
            assertEquals(sampleDecks, (successState as DecksState.Success).decks)
            assertEquals("", successState.searchQuery)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createDeck should call repository createDeck and succeed`() = runTest {
        // Given
        coEvery { deckRepository.getAllDecks() } returns flowOf(sampleDecks)
        coEvery { deckRepository.createDeck(any(), any()) } returns 4L
        viewModel = DecksViewModel(deckRepository)

        advanceUntilIdle()

        // When
        viewModel.createDeck("New Deck", "Description")

        advanceUntilIdle()

        // Then
        coVerify { deckRepository.createDeck("New Deck", "Description") }
    }

    @Test
    fun `createDeck should set error message when exception occurs`() = runTest {
        // Given
        coEvery { deckRepository.getAllDecks() } returns flowOf(sampleDecks)

        val exception = RuntimeException("Failed to create deck")
        coEvery { deckRepository.createDeck(any(), any()) } throws exception

        viewModel = DecksViewModel(deckRepository)

        advanceUntilIdle()

        viewModel.errorMessage.test {
            assertNull(awaitItem())

            viewModel.createDeck("New Deck", "Description")

            advanceUntilIdle()

            val errorMessage = awaitItem()
            assertEquals("Failed to create deck: Failed to create deck", errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteDeck should call repository deleteDeck and succeed`() = runTest {
        // Given
        val deckToDelete = sampleDecks[0]
        coEvery { deckRepository.getAllDecks() } returns flowOf(sampleDecks)
        coEvery { deckRepository.deleteDeck(any()) } returns Unit

        viewModel = DecksViewModel(deckRepository)

        advanceUntilIdle()

        // When
        viewModel.deleteDeck(deckToDelete)
        advanceUntilIdle()

        // Then
        coVerify { deckRepository.deleteDeck(deckToDelete.id) }

        viewModel.errorMessage.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteDeck should set error message when exception occurs`() = runTest {
        // Given
        val deckToDelete = sampleDecks[0]
        coEvery { deckRepository.getAllDecks() } returns flowOf(sampleDecks)
        val exception = RuntimeException("Failed to delete deck")

        coEvery { deckRepository.deleteDeck(any()) } throws exception
        viewModel = DecksViewModel(deckRepository)
        advanceUntilIdle()

        // When
        viewModel.deleteDeck(deckToDelete)
        advanceUntilIdle()

        // Then
        val currentError = viewModel.errorMessage.value
        assertEquals("Failed to delete deck: Failed to delete deck", currentError)
    }

    @Test
    fun `clearErrorMessage should reset error message to null`() = runTest {
        // Given
        coEvery { deckRepository.getAllDecks() } returns flowOf(sampleDecks)

        val exception = RuntimeException("Some error")
        coEvery { deckRepository.createDeck(any(), any()) } throws exception

        viewModel = DecksViewModel(deckRepository)

        advanceUntilIdle()

        viewModel.errorMessage.test {
            // Initial value: null
            assertNull(awaitItem())

            // Create error
            viewModel.createDeck("New Deck", null)
            advanceUntilIdle()

            // Error message
            val errorMessage = awaitItem()
            assertEquals("Failed to create deck: Some error", errorMessage)

            // Clear error
            viewModel.clearErrorMessage()
            advanceUntilIdle()

            // Cleared to null
            val clearedMessage = awaitItem()

            assertNull(clearedMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }
}