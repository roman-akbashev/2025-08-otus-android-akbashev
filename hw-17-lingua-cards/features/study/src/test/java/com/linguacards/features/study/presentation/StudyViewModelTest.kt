package com.linguacards.features.study.presentation

import androidx.lifecycle.SavedStateHandle
import com.linguacards.core.domain.repository.CardRepository
import com.linguacards.core.domain.usecase.CalculateNextReviewUseCase
import com.linguacards.core.model.Card
import com.linguacards.core.model.SrsGrade
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
class StudyViewModelTest {

    private lateinit var viewModel: StudyViewModel
    private lateinit var cardRepository: CardRepository
    private lateinit var calculateNextReviewUseCase: CalculateNextReviewUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private val testDispatcher = StandardTestDispatcher()

    private val now = Clock.System.now()
    private val deckId = 1L

    private val sampleCards = listOf(
        Card(
            id = 1,
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
        ),
        Card(
            id = 2,
            deckId = deckId,
            word = "Goodbye",
            translation = "До свидания",
            example = null,
            transcription = "ɡʊdˈbaɪ",
            easinessFactor = 2.5,
            interval = 3,
            repetitions = 1,
            nextReviewDate = now,
            createdAt = now,
            updatedAt = now
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        cardRepository = mockk()
        calculateNextReviewUseCase = mockk()
        savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load cards and show first card`() = runTest {
        // Given
        coEvery { cardRepository.getCardsForStudy(deckId, any()) } returns sampleCards

        // When
        viewModel = StudyViewModel(cardRepository, calculateNextReviewUseCase, savedStateHandle)
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertTrue(state is StudyState.Card)
        val cardState = state as StudyState.Card
        assertEquals(sampleCards[0].id, cardState.card.id)
        assertFalse(cardState.isFlipped)
        assertEquals("1/2", cardState.progress)
        assertFalse(cardState.isProcessing)
    }

    @Test
    fun `init when no cards available should set state to Finished`() = runTest {
        // Given
        coEvery { cardRepository.getCardsForStudy(deckId, any()) } returns emptyList()

        // When
        viewModel = StudyViewModel(cardRepository, calculateNextReviewUseCase, savedStateHandle)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.state.value is StudyState.Finished)
    }

    @Test
    fun `init when repository throws exception should set error message and Finished state`() =
        runTest {
            // Given
            coEvery {
                cardRepository.getCardsForStudy(
                    deckId,
                    any()
                )
            } throws RuntimeException("Network error")

            // When
            viewModel = StudyViewModel(cardRepository, calculateNextReviewUseCase, savedStateHandle)
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.state.value is StudyState.Finished)
            assertEquals("Network error", viewModel.errorMessage.value)
        }

    @Test
    fun `onCardFlip should toggle isFlipped state`() = runTest {
        // Given
        coEvery { cardRepository.getCardsForStudy(deckId, any()) } returns sampleCards
        viewModel = StudyViewModel(cardRepository, calculateNextReviewUseCase, savedStateHandle)
        advanceUntilIdle()

        val initialState = viewModel.state.value as StudyState.Card
        assertFalse(initialState.isFlipped)

        // When
        viewModel.onCardFlip()
        advanceUntilIdle()

        // Then
        val flippedState = viewModel.state.value as StudyState.Card
        assertTrue(flippedState.isFlipped)
    }

    @Test
    fun `onGradeSelected GOOD should update card using useCase and save`() = runTest {
        // Given
        coEvery { cardRepository.getCardsForStudy(deckId, any()) } returns sampleCards
        val currentCard = sampleCards[0]
        val newRepetitions = 3
        val newEf = 2.6
        val newInterval = 15

        coEvery {
            calculateNextReviewUseCase(
                currentCard.repetitions,
                currentCard.easinessFactor,
                currentCard.interval,
                SrsGrade.GOOD
            )
        } returns Triple(newRepetitions, newEf, newInterval)

        coEvery { cardRepository.updateCardAfterReview(any()) } returns Unit

        viewModel = StudyViewModel(cardRepository, calculateNextReviewUseCase, savedStateHandle)
        advanceUntilIdle()

        viewModel.onCardFlip()
        advanceUntilIdle()

        // When
        viewModel.onGradeSelected(SrsGrade.GOOD)
        advanceUntilIdle()

        // Then
        coVerify {
            calculateNextReviewUseCase(
                currentCard.repetitions,
                currentCard.easinessFactor,
                currentCard.interval,
                SrsGrade.GOOD
            )
        }
        coVerify { cardRepository.updateCardAfterReview(any()) }

        val state = viewModel.state.value
        assertTrue(state is StudyState.Card)
        val newState = state as StudyState.Card
        assertEquals(sampleCards[1].id, newState.card.id)
        assertEquals("2/2", newState.progress)
        assertFalse(newState.isProcessing)
    }

    @Test
    fun `onGradeSelected should not allow multiple simultaneous clicks (isProcessing flag)`() =
        runTest {
            // Given
            coEvery { cardRepository.getCardsForStudy(deckId, any()) } returns sampleCards
            coEvery {
                calculateNextReviewUseCase(any(), any(), any(), any())
            } answers {
                Thread.sleep(100)
                Triple(3, 2.6, 15)
            }
            coEvery { cardRepository.updateCardAfterReview(any()) } returns Unit

            viewModel = StudyViewModel(cardRepository, calculateNextReviewUseCase, savedStateHandle)
            advanceUntilIdle()
            viewModel.onCardFlip()
            advanceUntilIdle()

            // When
            viewModel.onGradeSelected(SrsGrade.GOOD)

            viewModel.onGradeSelected(SrsGrade.GOOD)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { cardRepository.updateCardAfterReview(any()) }
        }

    @Test
    fun `onGradeSelected when exception occurs should not remove card and show error`() = runTest {
        // Given
        coEvery { cardRepository.getCardsForStudy(deckId, any()) } returns sampleCards
        coEvery {
            calculateNextReviewUseCase(any(), any(), any(), any())
        } returns Triple(3, 2.6, 15)
        coEvery { cardRepository.updateCardAfterReview(any()) } throws RuntimeException("Save failed")

        viewModel = StudyViewModel(cardRepository, calculateNextReviewUseCase, savedStateHandle)
        advanceUntilIdle()
        viewModel.onCardFlip()
        advanceUntilIdle()

        // When
        viewModel.onGradeSelected(SrsGrade.GOOD)
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertTrue(state is StudyState.Card)
        val cardState = state as StudyState.Card
        assertEquals(sampleCards[0].id, cardState.card.id)
        assertFalse(cardState.isProcessing) // processing flag reset
        assertTrue(viewModel.errorMessage.value?.contains("Save failed") == true)
    }

    @Test
    fun `after last card study should set state to Finished`() = runTest {
        // Given
        coEvery { cardRepository.getCardsForStudy(deckId, any()) } returns listOf(sampleCards[0])
        coEvery {
            calculateNextReviewUseCase(any(), any(), any(), any())
        } returns Triple(3, 2.6, 15)
        coEvery { cardRepository.updateCardAfterReview(any()) } returns Unit

        viewModel = StudyViewModel(cardRepository, calculateNextReviewUseCase, savedStateHandle)
        advanceUntilIdle()
        viewModel.onCardFlip()
        advanceUntilIdle()

        // When
        viewModel.onGradeSelected(SrsGrade.GOOD)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.state.value is StudyState.Finished)
    }

    @Test
    fun `progress string should update correctly after each card`() = runTest {
        // Given
        coEvery { cardRepository.getCardsForStudy(deckId, any()) } returns sampleCards
        viewModel = StudyViewModel(cardRepository, calculateNextReviewUseCase, savedStateHandle)
        advanceUntilIdle()

        var state = viewModel.state.value as StudyState.Card
        assertEquals("1/2", state.progress)

        coEvery {
            calculateNextReviewUseCase(any(), any(), any(), any())
        } returns Triple(3, 2.6, 15)
        coEvery { cardRepository.updateCardAfterReview(any()) } returns Unit
        viewModel.onCardFlip()
        viewModel.onGradeSelected(SrsGrade.GOOD)
        advanceUntilIdle()

        state = viewModel.state.value as StudyState.Card
        assertEquals("2/2", state.progress)
    }

    @Test
    fun `clearErrorMessage should reset error message to null`() = runTest {
        // Given
        coEvery { cardRepository.getCardsForStudy(deckId, any()) } throws RuntimeException("Error")
        viewModel = StudyViewModel(cardRepository, calculateNextReviewUseCase, savedStateHandle)
        advanceUntilIdle()
        assertTrue(viewModel.errorMessage.value != null)

        // When
        viewModel.clearErrorMessage()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.errorMessage.value)
    }
}