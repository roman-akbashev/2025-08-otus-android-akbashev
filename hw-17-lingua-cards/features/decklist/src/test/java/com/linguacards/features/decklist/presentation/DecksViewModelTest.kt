package com.linguacards.features.decklist.presentation

import app.cash.turbine.test
import com.linguacards.core.domain.repository.DeckRepository
import com.linguacards.core.model.Deck
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DecksViewModelTest {

    private lateinit var viewModel: DecksViewModel
    private lateinit var deckRepository: DeckRepository

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        deckRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // MARK: - Test Data

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
            name = "French Grammar",
            description = "French grammar rules",
            createdAt = now,
            updatedAt = now,
            cardCount = 5
        ),
        Deck(
            id = 3,
            name = "German Vocabulary",
            description = "Common German words",
            createdAt = now,
            updatedAt = now,
            cardCount = 15
        )
    )

    // MARK: - Initial State Tests

    @Test
    fun `init should load decks and set Loading state initially`() = runTest {
        // Given
        val decksFlow = flowOf(sampleDecks)
        coEvery { deckRepository.getAllDecks() } returns decksFlow

        // When
        viewModel = DecksViewModel(deckRepository)

        // Then
        viewModel.state.test {
            // Should emit Loading first
            assertEquals(DecksState.Loading, awaitItem())

            // Then Success with decks
            val successState = awaitItem() as DecksState.Success
            assertEquals(sampleDecks, successState.decks)
            assertEquals("", successState.searchQuery)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init should handle empty decks and set Empty state`() = runTest {
        // Given
        val emptyDecksFlow = flowOf(emptyList<Deck>())
        coEvery { deckRepository.getAllDecks() } returns emptyDecksFlow

        // When
        viewModel = DecksViewModel(deckRepository)

        // Then
        viewModel.state.test {
            // Should emit Loading first
            assertEquals(DecksState.Loading, awaitItem())

            // Then Empty state
            assertEquals(DecksState.Empty, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init should handle repository error and set Error state`() = runTest {
        // Given
        val errorMessage = "Database error"
        val errorFlow = flow<List<Deck>> {
            throw RuntimeException(errorMessage)
        }
        coEvery { deckRepository.getAllDecks() } returns errorFlow

        // When
        viewModel = DecksViewModel(deckRepository)

        // Then
        viewModel.state.test {
            // Should emit Loading first
            assertEquals(DecksState.Loading, awaitItem())

            // Then Error state
            val errorState = awaitItem() as DecksState.Error
            assertEquals(errorMessage, errorState.message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // MARK: - Search Tests

    @Test
    fun `onSearchQueryChanged should filter decks by name`() = runTest {
        // Given
        val decksFlow = flowOf(sampleDecks)
        coEvery { deckRepository.getAllDecks() } returns decksFlow
        viewModel = DecksViewModel(deckRepository)

        // Wait for initial load
        viewModel.state.test {
            skipItems(2) // Skip Loading and initial Success
            cancel()
        }

        // When
        viewModel.onSearchQueryChanged("Spanish")

        // Then
        viewModel.state.test {
            val successState = awaitItem() as DecksState.Success
            assertEquals(1, successState.decks.size)
            assertEquals("Spanish Basics", successState.decks.first().name)
            assertEquals("Spanish", successState.searchQuery)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSearchQueryChanged should filter decks by description`() = runTest {
        // Given
        val decksFlow = flowOf(sampleDecks)
        coEvery { deckRepository.getAllDecks() } returns decksFlow
        viewModel = DecksViewModel(deckRepository)

        // Wait for initial load
        viewModel.state.test {
            skipItems(2)
            cancel()
        }

        // When
        viewModel.onSearchQueryChanged("grammar")

        // Then
        viewModel.state.test {
            val successState = awaitItem() as DecksState.Success
            assertEquals(1, successState.decks.size)
            assertEquals("French Grammar", successState.decks.first().name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSearchQueryChanged should be case insensitive`() = runTest {
        // Given
        val decksFlow = flowOf(sampleDecks)
        coEvery { deckRepository.getAllDecks() } returns decksFlow
        viewModel = DecksViewModel(deckRepository)

        // Wait for initial load
        viewModel.state.test {
            skipItems(2)
            cancel()
        }

        // When
        viewModel.onSearchQueryChanged("SPANISH")

        // Then
        viewModel.state.test {
            val successState = awaitItem() as DecksState.Success
            assertEquals(1, successState.decks.size)
            assertEquals("Spanish Basics", successState.decks.first().name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSearchQueryChanged with empty query should show all decks`() = runTest {
        // Given
        val decksFlow = flowOf(sampleDecks)
        coEvery { deckRepository.getAllDecks() } returns decksFlow
        viewModel = DecksViewModel(deckRepository)

        // Wait for initial load
        viewModel.state.test {
            skipItems(2)
            cancel()
        }

        // When
        viewModel.onSearchQueryChanged("Spanish")
        viewModel.onSearchQueryChanged("")

        // Then
        viewModel.state.test {
            // First search result
            val firstSearchState = awaitItem() as DecksState.Success
            assertEquals(1, firstSearchState.decks.size)

            // Then all decks after clearing search
            val allDecksState = awaitItem() as DecksState.Success
            assertEquals(3, allDecksState.decks.size)
            assertEquals("", allDecksState.searchQuery)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search with no matches should show empty success state`() = runTest {
        // Given
        val decksFlow = flowOf(sampleDecks)
        coEvery { deckRepository.getAllDecks() } returns decksFlow
        viewModel = DecksViewModel(deckRepository)

        // Wait for initial load
        viewModel.state.test {
            skipItems(2)
            cancel()
        }

        // When
        viewModel.onSearchQueryChanged("NonExistentDeck")

        // Then
        viewModel.state.test {
            val successState = awaitItem() as DecksState.Success
            assertTrue(successState.decks.isEmpty())
            assertEquals("NonExistentDeck", successState.searchQuery)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // MARK: - Deck Count Tests

    @Test
    fun `allDecksCount should update when decks change`() = runTest {
        // Given
        val decksFlow = flowOf(sampleDecks)
        coEvery { deckRepository.getAllDecks() } returns decksFlow
        viewModel = DecksViewModel(deckRepository)

        // Then
        viewModel.allDecksCount.test {
            assertEquals(3, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `allDecksCount should be zero for empty decks`() = runTest {
        // Given
        val emptyDecksFlow = flowOf(emptyList<Deck>())
        coEvery { deckRepository.getAllDecks() } returns emptyDecksFlow
        viewModel = DecksViewModel(deckRepository)

        // Then
        viewModel.allDecksCount.test {
            assertEquals(0, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // MARK: - Search Query State Tests

    @Test
    fun `searchQuery should update when onSearchQueryChanged is called`() = runTest {
        // Given
        val decksFlow = flowOf(sampleDecks)
        coEvery { deckRepository.getAllDecks() } returns decksFlow
        viewModel = DecksViewModel(deckRepository)

        // When
        viewModel.onSearchQueryChanged("test query")

        // Then
        viewModel.searchQuery.test {
            assertEquals("", awaitItem()) // Initial empty
            assertEquals("test query", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // MARK: - Create Deck Tests

    @Test
    fun `createDeck should call repository and refresh decks`() = runTest {
        // Given
        val decksFlow = flowOf(emptyList<Deck>())
        coEvery { deckRepository.getAllDecks() } returns decksFlow
        coEvery { deckRepository.createDeck("New Deck", "Description") } returns 1L

        viewModel = DecksViewModel(deckRepository)

        // When
        viewModel.createDeck("New Deck", "Description")

        // Then
        coVerify { deckRepository.createDeck("New Deck", "Description") }

        // Note: The list will update automatically through the Flow
        // We can't easily test the refresh without mocking the Flow update
    }

    @Test
    fun `createDeck should set error state on failure`() = runTest {
        // Given
        val decksFlow = flowOf(emptyList<Deck>())
        coEvery { deckRepository.getAllDecks() } returns decksFlow
        coEvery {
            deckRepository.createDeck(
                "New Deck",
                "Description"
            )
        } throws RuntimeException("Creation failed")

        viewModel = DecksViewModel(deckRepository)

        // When
        viewModel.createDeck("New Deck", "Description")

        // Give time for the coroutine to execute
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            // Skip initial states
            skipItems(2)

            // Should have error state
            val errorState = awaitItem() as DecksState.Error
            assertTrue(errorState.message.contains("Failed to create deck"))
            assertTrue(errorState.message.contains("Creation failed"))

            cancelAndIgnoreRemainingEvents()
        }
    }

    // MARK: - Delete Deck Tests

    @Test
    fun `deleteDeck should call repository and refresh decks`() = runTest {
        // Given
        val decksFlow = flowOf(sampleDecks)
        coEvery { deckRepository.getAllDecks() } returns decksFlow
        coEvery { deckRepository.deleteDeck(1L) } returns Unit

        viewModel = DecksViewModel(deckRepository)

        // When
        viewModel.deleteDeck(sampleDecks[0])

        // Then
        coVerify { deckRepository.deleteDeck(1L) }
    }

    @Test
    fun `deleteDeck should set error state on failure`() = runTest {
        // Given
        val decksFlow = flowOf(sampleDecks)
        coEvery { deckRepository.getAllDecks() } returns decksFlow
        coEvery { deckRepository.deleteDeck(1L) } throws RuntimeException("Deletion failed")

        viewModel = DecksViewModel(deckRepository)

        // When
        viewModel.deleteDeck(sampleDecks[0])

        // Give time for the coroutine to execute
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            // Skip initial states
            skipItems(2)

            // Should have error state
            val errorState = awaitItem() as DecksState.Error
            assertTrue(errorState.message.contains("Failed to delete deck"))
            assertTrue(errorState.message.contains("Deletion failed"))

            cancelAndIgnoreRemainingEvents()
        }
    }

    // MARK: - Retry Tests

    @Test
    fun `retry should reload decks`() = runTest {
        // Given
        val initialDecks = flowOf(emptyList<Deck>())
        val updatedDecks = flowOf(sampleDecks)

        coEvery { deckRepository.getAllDecks() } returns initialDecks andThen updatedDecks

        viewModel = DecksViewModel(deckRepository)

        // Wait for initial empty state
        viewModel.state.test {
            skipItems(2) // Skip Loading and Empty
            cancel()
        }

        // When
        viewModel.retry()

        // Then
        viewModel.state.test {
            // Should emit Loading again
            assertEquals(DecksState.Loading, awaitItem())

            // Then Success with decks
            val successState = awaitItem() as DecksState.Success
            assertEquals(sampleDecks, successState.decks)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // MARK: - Real-time Updates Tests

    @Test
    fun `should update state when repository flow emits new data`() = runTest {
        // Given
        val decksFlow = MutableStateFlow(emptyList<Deck>())
        coEvery { deckRepository.getAllDecks() } returns decksFlow

        viewModel = DecksViewModel(deckRepository)

        // Initial empty state
        viewModel.state.test {
            skipItems(2) // Skip Loading and Empty
            cancel()
        }

        // When
        decksFlow.value = sampleDecks

        // Then
        viewModel.state.test {
            val successState = awaitItem() as DecksState.Success
            assertEquals(sampleDecks, successState.decks)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should maintain search filter when decks update`() = runTest {
        // Given
        val decksFlow = MutableStateFlow(emptyList<Deck>())
        coEvery { deckRepository.getAllDecks() } returns decksFlow

        viewModel = DecksViewModel(deckRepository)

        // Set search query
        viewModel.onSearchQueryChanged("Spanish")

        // Initial state
        viewModel.state.test {
            skipItems(2)
            cancel()
        }

        // When
        decksFlow.value = sampleDecks

        // Then
        viewModel.state.test {
            val successState = awaitItem() as DecksState.Success
            assertEquals(1, successState.decks.size)
            assertEquals("Spanish Basics", successState.decks.first().name)
            assertEquals("Spanish", successState.searchQuery)

            cancelAndIgnoreRemainingEvents()
        }
    }
}