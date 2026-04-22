package com.linguacards.features.cardedit.presentation

import androidx.lifecycle.SavedStateHandle
import com.linguacards.core.domain.repository.CardRepository
import com.linguacards.core.model.Card
import com.linguacards.core.model.WordDetails
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardEditViewModelTest {

    private lateinit var viewModel: CardEditViewModel
    private lateinit var cardRepository: CardRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private val testDispatcher = StandardTestDispatcher()

    private val now = Clock.System.now()
    private val deckId = 1L
    private val cardId = 100L

    private val sampleCard = Card(
        id = cardId,
        deckId = deckId,
        word = "Hello",
        translation = "Привет",
        example = "Hello world",
        transcription = "həˈləʊ",
        easinessFactor = 2.5,
        interval = 5,
        repetitions = 2,
        nextReviewDate = now,
        createdAt = now,
        updatedAt = now
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        cardRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `init without cardId should create empty Content state`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))

        // When
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)

        // Then
        val state = viewModel.state.value
        assertTrue(state is CardEditState.Content)
        val content = state as CardEditState.Content
        assertEquals(deckId, content.deckId)
        assertEquals(0L, content.cardId)
        assertFalse(content.isEditing)
        assertEquals("", content.word)
        assertEquals("", content.translation)
        assertEquals("", content.example)
        assertEquals("", content.transcription)
        assertTrue(content.errors.isEmpty())
        assertNull(content.originalCard)
    }

    @Test
    fun `init with existing cardId should load card and update state`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId, "cardId" to cardId))
        coEvery { cardRepository.getCardById(cardId) } returns sampleCard

        // When
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertTrue(state is CardEditState.Content)
        val content = state as CardEditState.Content
        assertEquals(deckId, content.deckId)
        assertEquals(cardId, content.cardId)
        assertTrue(content.isEditing)
        assertEquals(sampleCard.word, content.word)
        assertEquals(sampleCard.translation, content.translation)
        assertEquals(sampleCard.example, content.example)
        assertEquals(sampleCard.transcription, content.transcription)
        assertEquals(sampleCard, content.originalCard)
    }

    @Test
    fun `init with invalid cardId should set error message`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId, "cardId" to cardId))
        coEvery { cardRepository.getCardById(cardId) } returns null

        // When
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Card not found", viewModel.errorMessage.value)
    }

    @Test
    fun `init with repository exception should set error message`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId, "cardId" to cardId))
        coEvery { cardRepository.getCardById(cardId) } throws RuntimeException("DB error")

        // When
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains("DB error") == true)
    }

    @Test
    fun `onWordChanged should update word and clear word error`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)

        // Simulate a validation error first
        viewModel.onSaveClick() // triggers validation
        testDispatcher.scheduler.advanceUntilIdle()

        val stateWithError = viewModel.state.value as CardEditState.Content
        assertTrue(stateWithError.errors.containsKey(ValidationErrorField.WORD))

        // When
        viewModel.onWordChanged("New Word")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val newState = viewModel.state.value as CardEditState.Content
        assertEquals("New Word", newState.word)
        assertFalse(newState.errors.containsKey(ValidationErrorField.WORD))
    }

    @Test
    fun `onTranslationChanged should update translation and clear translation error`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)

        viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        val stateWithError = viewModel.state.value as CardEditState.Content
        assertTrue(stateWithError.errors.containsKey(ValidationErrorField.TRANSLATION))

        // When
        viewModel.onTranslationChanged("Новый перевод")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val newState = viewModel.state.value as CardEditState.Content
        assertEquals("Новый перевод", newState.translation)
        assertFalse(newState.errors.containsKey(ValidationErrorField.TRANSLATION))
    }

    @Test
    fun `onExampleChanged should update example`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)

        // When
        viewModel.onExampleChanged("New example sentence")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value as CardEditState.Content
        assertEquals("New example sentence", state.example)
    }

    @Test
    fun `onTranscriptionChanged should update transcription`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)

        // When
        viewModel.onTranscriptionChanged("/njuː/")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value as CardEditState.Content
        assertEquals("/njuː/", state.transcription)
    }

    @Test
    fun `onSaveClick with empty word and translation should set errors and return false`() =
        runTest {
            // Given
            savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
            viewModel = CardEditViewModel(cardRepository, savedStateHandle)

            // When
            val result = viewModel.onSaveClick()
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            assertFalse(result)
            val state = viewModel.state.value as CardEditState.Content
            assertTrue(state.errors.containsKey(ValidationErrorField.WORD))
            assertTrue(state.errors.containsKey(ValidationErrorField.TRANSLATION))
        }

    @Test
    fun `onSaveClick for new card should call createCard and emit Saved state`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
        coEvery { cardRepository.createCard(any()) } returns 123L
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)

        viewModel.onWordChanged("Apple")
        viewModel.onTranslationChanged("Яблоко")
        viewModel.onExampleChanged("An apple a day")
        viewModel.onTranscriptionChanged("ˈæpəl")

        // When
        val result = viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(result)
        coVerify { cardRepository.createCard(any()) }
        assertTrue(viewModel.state.value is CardEditState.Saved)
    }

    @Test
    fun `onSaveClick for existing card should call updateCard and emit Saved state`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId, "cardId" to cardId))
        coEvery { cardRepository.getCardById(cardId) } returns sampleCard
        coEvery { cardRepository.updateCard(any()) } returns Unit
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onWordChanged("Updated Word")
        viewModel.onTranslationChanged("Обновленный перевод")

        // When
        val result = viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(result)
        coVerify { cardRepository.updateCard(any()) }
        assertTrue(viewModel.state.value is CardEditState.Saved)
    }

    @Test
    fun `onSaveClick when repository throws exception should set error message`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
        coEvery { cardRepository.createCard(any()) } throws RuntimeException("Save failed")
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)

        viewModel.onWordChanged("Word")
        viewModel.onTranslationChanged("Translation")

        // When
        viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains("Save failed") == true)
    }

    @Test
    fun `onWordFocusLost should fetch details and update fields if empty`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
        val word = "TestWord"
        val details = WordDetails(word, "ˈtest", "This is a test example")
        coEvery { cardRepository.fetchWordDetails(word) } returns Result.success(details)

        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        viewModel.onWordChanged(word)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onWordFocusLost()
        // advance time by debounce delay (500 ms)
        testDispatcher.scheduler.advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value as CardEditState.Content
        assertEquals(details.transcription, state.transcription)
        assertEquals(details.example, state.example)
        assertFalse(state.isFetchingDetails)
    }

    @Test
    fun `onWordFocusLost should not fetch if word is blank`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        viewModel.onWordChanged("   ")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onWordFocusLost()
        testDispatcher.scheduler.advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { cardRepository.fetchWordDetails(any()) }
    }

    @Test
    fun `onWordFocusLost should not fetch if example and transcription already filled`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        viewModel.onWordChanged("Hello")
        viewModel.onExampleChanged("Existing example")
        viewModel.onTranscriptionChanged("Existing transcription")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onWordFocusLost()
        testDispatcher.scheduler.advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { cardRepository.fetchWordDetails(any()) }
    }

    @Test
    fun `onWordFocusLost should handle fetch error and show error message`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
        val word = "NotFound"
        coEvery { cardRepository.fetchWordDetails(word) } returns Result.failure(Exception("Word not found"))

        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        viewModel.onWordChanged(word)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onWordFocusLost()
        testDispatcher.scheduler.advanceTimeBy(500)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.errorMessage.value?.contains("Word not found") == true)
        val state = viewModel.state.value as CardEditState.Content
        assertFalse(state.isFetchingDetails)
    }


    @Test
    fun `resetState should revert to original card values`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId, "cardId" to cardId))
        coEvery { cardRepository.getCardById(cardId) } returns sampleCard
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        // Modify fields
        viewModel.onWordChanged("Modified")
        viewModel.onTranslationChanged("Модифицировано")
        viewModel.onExampleChanged("Modified example")
        viewModel.onTranscriptionChanged("modified")

        // When
        viewModel.resetState()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.value as CardEditState.Content
        assertEquals(sampleCard.word, state.word)
        assertEquals(sampleCard.translation, state.translation)
        assertEquals(sampleCard.example, state.example)
        assertEquals(sampleCard.transcription, state.transcription)
        assertTrue(state.errors.isEmpty())
    }

    @Test
    fun `clearErrorMessage should set errorMessage to null`() = runTest {
        // Given
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
        coEvery { cardRepository.createCard(any()) } throws RuntimeException("Error")
        viewModel = CardEditViewModel(cardRepository, savedStateHandle)

        viewModel.onWordChanged("Word")
        viewModel.onTranslationChanged("Translation")
        viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.errorMessage.value != null)

        // When
        viewModel.clearErrorMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.errorMessage.value)
    }
}