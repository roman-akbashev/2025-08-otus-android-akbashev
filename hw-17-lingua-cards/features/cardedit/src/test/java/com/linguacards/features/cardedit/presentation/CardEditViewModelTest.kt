package com.linguacards.features.cardedit.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.linguacards.core.domain.repository.CardRepository
import com.linguacards.core.model.Card
import com.linguacards.core.model.WordDetails
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class CardEditViewModelTest {

    private lateinit var viewModel: CardEditViewModel
    private lateinit var cardRepository: CardRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var testDispatcher: TestDispatcher

    private val testDeckId = 1L
    private val testCardId = 100L
    private val now = Clock.System.now()

    private val sampleCard = Card(
        id = testCardId,
        deckId = testDeckId,
        word = "apple",
        translation = "яблоко",
        example = "I eat an apple",
        transcription = "/ˈæpəl/",
        easinessFactor = 2.5,
        interval = 0,
        repetitions = 0,
        nextReviewDate = null,
        createdAt = now,
        updatedAt = now
    )

    private val wordDetails = WordDetails(
        word = "apple",
        example = "This is an example sentence",
        transcription = "/ˈæpəl/",
    )

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        cardRepository = mockk()
        savedStateHandle = SavedStateHandle(
            mapOf(
                "deckId" to testDeckId,
                "cardId" to testCardId
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init with new card (cardId = 0) should set initial Content state without loading card`() = runTest {
        // Given
        val newCardHandle = SavedStateHandle(mapOf("deckId" to testDeckId, "cardId" to 0L))

        // When
        viewModel = CardEditViewModel(cardRepository, newCardHandle)

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state is CardEditState.Content)
            assertEquals(testDeckId, (state as CardEditState.Content).deckId)
            assertEquals(0L, state.cardId)
            assertFalse(state.isEditing)
            assertEquals("", state.word)
            assertEquals("", state.translation)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init with existing card (cardId != 0) should set initial state and load card`() = runTest {
        // Given
        coEvery { cardRepository.getCardById(testCardId) } returns sampleCard

        // When
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)

        // Then
        viewModel.state.test {
            // Initial Content state (empty fields)
            val initialState = awaitItem()
            assertTrue(initialState is CardEditState.Content)
            assertEquals(testDeckId, (initialState as CardEditState.Content).deckId)
            assertEquals(testCardId, initialState.cardId)
            assertTrue(initialState.isEditing)
            assertEquals("", initialState.word) // Still empty initially

            // Wait for card to load
            advanceUntilIdle()

            // Updated state with loaded card data
            val loadedState = awaitItem()
            assertTrue(loadedState is CardEditState.Content)
            assertEquals("apple", (loadedState as CardEditState.Content).word)
            assertEquals("яблоко", loadedState.translation)
            assertEquals("I eat an apple", loadedState.example)
            assertEquals("/ˈæpəl/", loadedState.transcription)
            assertEquals(sampleCard, loadedState.originalCard)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init with existing card should set error message when card not found`() = runTest {
        // Given
        coEvery { cardRepository.getCardById(testCardId) } returns null

        // When
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)

        // Then
        viewModel.errorMessage.test {
            assertNull(awaitItem())

            advanceUntilIdle()

            val errorMessage = awaitItem()
            assertEquals("Card not found", errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init with existing card should set error message when repository throws exception`() = runTest {
        // Given
        coEvery { cardRepository.getCardById(testCardId) } throws RuntimeException("Database error")

        // When
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)

        // Then
        viewModel.errorMessage.test {
            assertNull(awaitItem())

            advanceUntilIdle()

            val errorMessage = awaitItem()
            assertEquals("Failed to load card: Database error", errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onWordChanged should update word and clear word error`() = runTest {
        // Given
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.state.test {
            val initialState = awaitItem() as CardEditState.Content
            assertEquals("", initialState.word)
            assertTrue(initialState.errors.isEmpty())

            // When - set word
            viewModel.onWordChanged("new word")

            val updatedState = awaitItem() as CardEditState.Content
            assertEquals("new word", updatedState.word)

            // When - simulate error then fix
            viewModel.onSaveClick() // This would set error if validation fails
            advanceUntilIdle()

            val stateWithError = awaitItem() as CardEditState.Content
            assertTrue(stateWithError.errors.containsKey("word"))

            // Fix word
            viewModel.onWordChanged("fixed word")
            val stateErrorCleared = awaitItem() as CardEditState.Content
            assertFalse(stateErrorCleared.errors.containsKey("word"))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onTranslationChanged should update translation and clear translation error`() = runTest {
        // Given
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.state.test {
            val initialState = awaitItem() as CardEditState.Content
            assertEquals("", initialState.translation)

            // When
            viewModel.onTranslationChanged("перевод")

            val updatedState = awaitItem() as CardEditState.Content
            assertEquals("перевод", updatedState.translation)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onExampleChanged should update example`() = runTest {
        // Given
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem() // Initial state

            // When
            viewModel.onExampleChanged("New example sentence")

            val updatedState = awaitItem() as CardEditState.Content
            assertEquals("New example sentence", updatedState.example)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onTranscriptionChanged should update transcription`() = runTest {
        // Given
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem() // Initial state

            // When
            viewModel.onTranscriptionChanged("/njuː/")

            val updatedState = awaitItem() as CardEditState.Content
            assertEquals("/njuː/", updatedState.transcription)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onWordFocusLost should fetch word details after debounce when word is not blank`() = runTest {
        // Given
        coEvery { cardRepository.getCardById(any()) } returns null
        coEvery { cardRepository.fetchWordDetails("apple") } returns Result.success(wordDetails)

        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.state.test {
            val initialState = awaitItem() as CardEditState.Content

            // When
            viewModel.onWordChanged("apple")
            awaitItem() // Word updated

            viewModel.onWordFocusLost()

            // Initially isFetchingDetails should be false
            assertFalse(initialState.isFetchingDetails)

            // Advance time by 500ms (debounce delay)
            advanceTimeBy(500.milliseconds)

            // Should be fetching details
            val fetchingState = awaitItem() as CardEditState.Content
            assertTrue(fetchingState.isFetchingDetails)

            advanceUntilIdle()

            // Details fetched
            val finalState = awaitItem() as CardEditState.Content
            assertFalse(finalState.isFetchingDetails)
            assertEquals("This is an example sentence", finalState.example)
            assertEquals("/ˈæpəl/", finalState.transcription)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onWordFocusLost should not fetch details when word is blank`() = runTest {
        // Given
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        advanceUntilIdle()

        // When
        viewModel.onWordFocusLost()
        advanceTimeBy(500.milliseconds)
        advanceUntilIdle()

        // Then - no fetch should happen, verify no calls to fetchWordDetails
        coVerify(exactly = 0) { cardRepository.fetchWordDetails(any()) }
    }

    @Test
    fun `onWordFocusLost should cancel previous debounce when called multiple times`() = runTest {
        // Given
        coEvery { cardRepository.getCardById(any()) } returns null
        coEvery { cardRepository.fetchWordDetails(any()) } returns Result.success(wordDetails)

        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        advanceUntilIdle()

        // When - call focus lost multiple times with different words
        viewModel.onWordChanged("apple")
        viewModel.onWordFocusLost()

        // Before debounce finishes, change word again
        advanceTimeBy(200.milliseconds)
        viewModel.onWordChanged("banana")
        viewModel.onWordFocusLost()

        // Wait for debounce
        advanceTimeBy(500.milliseconds)
        advanceUntilIdle()

        // Then - only the last word should be fetched
        coVerify(exactly = 1) { cardRepository.fetchWordDetails("banana") }
        coVerify(exactly = 0) { cardRepository.fetchWordDetails("apple") }
    }

    @Test
    fun `onWordFocusLost should not overwrite existing example and transcription if they are not empty`() = runTest {
        // Given
        coEvery { cardRepository.getCardById(any()) } returns null
        coEvery { cardRepository.fetchWordDetails("apple") } returns Result.success(wordDetails)

        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem() // Initial

            // Set existing values
            viewModel.onExampleChanged("User typed example")
            viewModel.onTranscriptionChanged("/user/")
            awaitItem() // Example updated
            awaitItem() // Transcription updated

            // When
            viewModel.onWordChanged("apple")
            awaitItem()
            viewModel.onWordFocusLost()
            advanceTimeBy(500.milliseconds)
            advanceUntilIdle()

            // Then - existing values should NOT be overwritten
            val finalState = awaitItem() as CardEditState.Content
            assertEquals("User typed example", finalState.example)
            assertEquals("/user/", finalState.transcription)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onWordFocusLost should handle fetch failure gracefully`() = runTest {
        // Given
        coEvery { cardRepository.getCardById(any()) } returns null
        coEvery { cardRepository.fetchWordDetails("unknown") } returns Result.failure(Exception("Word not found"))

        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem() // Initial

            // When
            viewModel.onWordChanged("unknown")
            awaitItem()
            viewModel.onWordFocusLost()

            advanceTimeBy(500.milliseconds)

            // Should show fetching
            val fetchingState = awaitItem() as CardEditState.Content
            assertTrue(fetchingState.isFetchingDetails)

            advanceUntilIdle()

            // Should stop fetching without updating fields
            val finalState = awaitItem() as CardEditState.Content
            assertFalse(finalState.isFetchingDetails)
            // Fields should remain empty (or whatever they were)
            assertEquals("", finalState.example)
            assertEquals("", finalState.transcription)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSaveClick should return false and set errors when word is blank`() = runTest {
        // Given
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.state.test {
            val initialState = awaitItem() as CardEditState.Content
            assertTrue(initialState.errors.isEmpty())

            // When
            val result = viewModel.onSaveClick()

            // Then
            assertFalse(result)

            val stateWithErrors = awaitItem() as CardEditState.Content
            assertTrue(stateWithErrors.errors.containsKey("word"))
            assertEquals("Word is required", stateWithErrors.errors["word"])

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSaveClick should return false and set errors when translation is blank`() = runTest {
        // Given
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        advanceUntilIdle()

        viewModel.state.test {
            awaitItem() // Initial

            // Set word but not translation
            viewModel.onWordChanged("apple")
            awaitItem()

            // When
            val result = viewModel.onSaveClick()

            // Then
            assertFalse(result)

            val stateWithErrors = awaitItem() as CardEditState.Content
            assertTrue(stateWithErrors.errors.containsKey("translation"))
            assertEquals("Translation is required", stateWithErrors.errors["translation"])

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSaveClick should return true and save new card when validation passes`() = runTest {
        // Given
        coEvery { cardRepository.getCardById(any()) } returns null
        coEvery { cardRepository.createCard(any()) } returns 200L

        val newCardHandle = SavedStateHandle(mapOf("deckId" to testDeckId, "cardId" to 0L))
        viewModel = CardEditViewModel(cardRepository, newCardHandle)
        advanceUntilIdle()

        // Fill valid data
        viewModel.onWordChanged("apple")
        viewModel.onTranslationChanged("яблоко")

        viewModel.state.test {
            awaitItem() // Initial
            awaitItem() // Word updated
            awaitItem() // Translation updated

            // When
            val result = viewModel.onSaveClick()

            // Then
            assertTrue(result)

            // Should emit Saved state
            val savedState = awaitItem()
            assertTrue(savedState is CardEditState.Saved)

            coVerify(exactly = 1) { cardRepository.createCard(any()) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSaveClick should return true and update existing card when editing`() = runTest {
        // Given
        coEvery { cardRepository.getCardById(testCardId) } returns sampleCard
        coEvery { cardRepository.updateCard(any()) } returns Unit

        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        advanceUntilIdle()
        advanceUntilIdle() // Load card

        viewModel.state.test {
            awaitItem() // Initial
            val loadedState = awaitItem() as CardEditState.Content
            assertEquals("apple", loadedState.word)

            // Update word
            viewModel.onWordChanged("updated apple")
            awaitItem()

            // When
            val result = viewModel.onSaveClick()

            // Then
            assertTrue(result)

            awaitItem() // Saved state

            coVerify(exactly = 1) { cardRepository.updateCard(any()) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSaveClick should set error message when save fails`() = runTest {
        // Given
        coEvery { cardRepository.getCardById(any()) } returns null
        coEvery { cardRepository.createCard(any()) } throws RuntimeException("Save failed")

        val newCardHandle = SavedStateHandle(mapOf("deckId" to testDeckId, "cardId" to 0L))
        viewModel = CardEditViewModel(cardRepository, newCardHandle)
        advanceUntilIdle()

        viewModel.errorMessage.test {
            assertNull(awaitItem())

            // Fill valid data and save
            viewModel.onWordChanged("apple")
            viewModel.onTranslationChanged("яблоко")
            viewModel.onSaveClick()

            advanceUntilIdle()

            val errorMessage = awaitItem()
            assertEquals("Failed to save card: Save failed", errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetState should restore original card values when editing`() = runTest {
        // Given
        coEvery { cardRepository.getCardById(testCardId) } returns sampleCard

        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        advanceUntilIdle()
        advanceUntilIdle() // Load card

        viewModel.state.test {
            val loadedState = awaitItem() as CardEditState.Content
            assertEquals("apple", loadedState.word)

            // Modify fields
            viewModel.onWordChanged("modified")
            viewModel.onTranslationChanged("изменено")
            viewModel.onExampleChanged("Modified example")
            awaitItem() // word
            awaitItem() // translation
            awaitItem() // example

            val modifiedState = viewModel.state.value as CardEditState.Content
            assertEquals("modified", modifiedState.word)
            assertEquals("изменено", modifiedState.translation)

            // When - reset
            viewModel.resetState()

            // Then
            val resetState = awaitItem() as CardEditState.Content
            assertEquals("apple", resetState.word)
            assertEquals("яблоко", resetState.translation)
            assertEquals("I eat an apple", resetState.example)
            assertEquals("/ˈæpəl/", resetState.transcription)
            assertTrue(resetState.errors.isEmpty())
            assertFalse(resetState.isFetchingDetails)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetState should clear fields for new card`() = runTest {
        // Given
        val newCardHandle = SavedStateHandle(mapOf("deckId" to testDeckId, "cardId" to 0L))
        viewModel = CardEditViewModel(cardRepository, newCardHandle)
        advanceUntilIdle()

        // Fill some data
        viewModel.onWordChanged("some word")
        viewModel.onTranslationChanged("some translation")
        viewModel.onExampleChanged("some example")

        viewModel.state.test {
            awaitItem() // Initial
            awaitItem() // word
            awaitItem() // translation
            awaitItem() // example

            // When
            viewModel.resetState()

            // Then
            val resetState = awaitItem() as CardEditState.Content
            assertEquals("", resetState.word)
            assertEquals("", resetState.translation)
            assertEquals("", resetState.example)
            assertEquals("", resetState.transcription)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearErrorMessage should reset error message to null`() = runTest {
        // Given - create an error
        coEvery { cardRepository.getCardById(any()) } returns null
        coEvery { cardRepository.createCard(any()) } throws RuntimeException("Test error")

        val newCardHandle = SavedStateHandle(mapOf("deckId" to testDeckId, "cardId" to 0L))
        viewModel = CardEditViewModel(cardRepository, newCardHandle)
        advanceUntilIdle()

        viewModel.errorMessage.test {
            assertNull(awaitItem())

            // Create error
            viewModel.onWordChanged("word")
            viewModel.onTranslationChanged("translation")
            viewModel.onSaveClick()
            advanceUntilIdle()

            val errorMessage = awaitItem()
            assertEquals("Failed to save card: Test error", errorMessage)

            // Clear error
            viewModel.clearErrorMessage()
            advanceUntilIdle()

            val clearedMessage = awaitItem()
            assertNull(clearedMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }
}