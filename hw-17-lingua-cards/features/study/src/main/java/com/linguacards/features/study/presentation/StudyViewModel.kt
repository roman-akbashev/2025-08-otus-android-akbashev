package com.linguacards.features.study.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguacards.core.domain.repository.CardRepository
import com.linguacards.core.domain.usecase.CalculateNextReviewUseCase
import com.linguacards.core.model.Card
import com.linguacards.core.model.SrsGrade
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val calculateNextReviewUseCase: CalculateNextReviewUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val deckId: Long = savedStateHandle.get<Long>("deckId") ?: 0L
    private val _state = MutableStateFlow<StudyState>(StudyState.Loading)
    val state: StateFlow<StudyState> = _state.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private var currentCard: Card? = null
    private val cardsQueue = mutableListOf<Card>()
    private var totalCards = 0

    init {
        loadCards()
    }

    fun loadCards() {
        viewModelScope.launch {
            try {
                val cards = cardRepository.getCardsForStudy(deckId)
                cardsQueue.clear()
                cardsQueue.addAll(cards)
                totalCards = cardsQueue.size
                showNextCard()
            } catch (e: Exception) {
                _state.update { StudyState.Finished }
                _errorMessage.update { e.message }
            }
        }
    }

    fun onCardFlip() {
        val currentState = _state.value
        if (currentState is StudyState.Card) {
            _state.update {
                currentState.copy(isFlipped = !currentState.isFlipped)
            }
        }
    }

    fun onGradeSelected(grade: SrsGrade) {
        val currentState = _state.value
        // Блокируем повторные вызовы, если уже обрабатывается
        if (currentState !is StudyState.Card || currentState.isProcessing) return

        // Устанавливаем флаг обработки
        _state.update { currentState.copy(isProcessing = true) }

        viewModelScope.launch {
            try {
                val card = currentState.card
                // Вычисляем новые значения по SM-2
                val (newRepetitions, newEf, newInterval) = calculateNextReviewUseCase(
                    card.repetitions,
                    card.easinessFactor,
                    card.interval,
                    grade
                )

                // Обновляем карточку
                val updatedCard = card.copy(
                    repetitions = newRepetitions,
                    easinessFactor = newEf,
                    interval = newInterval,
                    nextReviewDate = Clock.System.now().plus(newInterval.days),
                    updatedAt = Clock.System.now()
                )

                // Сохраняем в БД
                cardRepository.updateCardAfterReview(updatedCard)

                // Удаляем карточку из очереди ТОЛЬКО после успешного сохранения
                cardsQueue.removeFirstOrNull()

                // Показываем следующую карточку
                showNextCard()
            } catch (e: Exception) {
                // В случае ошибки сбрасываем флаг обработки, но не удаляем карточку из очереди
                // Пользователь может попробовать снова
                _state.update { currentState.copy(isProcessing = false) }
                _errorMessage.update { e.message }
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.update { null }
    }


    private fun showNextCard() {
        if (cardsQueue.isEmpty()) {
            _state.update { StudyState.Finished }
        } else {
            currentCard = cardsQueue.first()
            val studiedCount = totalCards - cardsQueue.size + 1
            _state.update {
                StudyState.Card(
                    card = currentCard!!,
                    isFlipped = false,
                    progress = "$studiedCount/$totalCards"
                )
            }
        }
    }
}
