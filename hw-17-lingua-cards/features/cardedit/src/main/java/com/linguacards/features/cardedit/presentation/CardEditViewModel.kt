package com.linguacards.features.cardedit.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguacards.core.domain.repository.CardRepository
import com.linguacards.core.model.Card
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class CardEditViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val deckId: Long = savedStateHandle.get<Long>("deckId") ?: 0L
    private val cardId: Long = savedStateHandle.get<Long>("cardId") ?: 0L

    private val _state = MutableStateFlow<CardEditState>(
        CardEditState.Content(
            deckId = deckId,
            cardId = cardId,
            isEditing = cardId != 0L
        )
    )
    val state: StateFlow<CardEditState> = _state.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var debounceJob: Job? = null

    init {
        if (cardId != 0L) {
            loadCard()
        }
    }

    private fun loadCard() {
        viewModelScope.launch {
            try {
                val card = cardRepository.getCardById(cardId)
                if (card != null) {
                    _state.update { currentState ->
                        if (currentState is CardEditState.Content) {
                            currentState.copy(
                                word = card.word,
                                translation = card.translation,
                                example = card.example ?: "",
                                transcription = card.transcription ?: "",
                                originalCard = card
                            )
                        } else currentState
                    }
                } else {
                    _errorMessage.update { "Card not found" }
                }
            } catch (e: Exception) {
                _errorMessage.update { "Failed to load card: ${e.message}" }
            }
        }
    }

    fun onWordChanged(word: String) {
        _state.update { currentState ->
            if (currentState is CardEditState.Content) {
                currentState.copy(
                    word = word,
                    errors = currentState.errors - ValidationErrorField.WORD
                )
            } else currentState
        }
    }

    fun onTranslationChanged(translation: String) {
        _state.update { currentState ->
            if (currentState is CardEditState.Content) {
                currentState.copy(
                    translation = translation,
                    errors = currentState.errors - ValidationErrorField.TRANSLATION
                )
            } else currentState
        }
    }

    fun onExampleChanged(example: String) {
        _state.update { currentState ->
            if (currentState is CardEditState.Content) {
                currentState.copy(example = example)
            } else currentState
        }
    }

    fun onTranscriptionChanged(transcription: String) {
        _state.update { currentState ->
            if (currentState is CardEditState.Content) {
                currentState.copy(transcription = transcription)
            } else currentState
        }
    }

    fun onWordFocusLost() {
        val currentState = _state.value
        if (currentState is CardEditState.Content) {
            val word = currentState.word.trim()

            if (word.isBlank() ||
                (currentState.example.isNotBlank() && currentState.transcription.isNotBlank())
            ) {
                return
            }

            // Запускаем загрузку с дебаунсом
            debounceJob?.cancel()
            debounceJob = viewModelScope.launch {
                delay(500)
                fetchWordDetails(word)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        debounceJob?.cancel()
    }

    private fun fetchWordDetails(word: String) {
        viewModelScope.launch {
            _state.update { currentState ->
                if (currentState is CardEditState.Content) {
                    currentState.copy(isFetchingDetails = true)
                } else currentState
            }

            val result = cardRepository.fetchWordDetails(word)
            result.onSuccess { details ->
                _state.update { currentState ->
                    if (currentState is CardEditState.Content) {
                        currentState.copy(
                            example = currentState.example.ifEmpty { details.example ?: "" },
                            transcription = currentState.transcription.ifEmpty {
                                details.transcription ?: ""
                            },
                            isFetchingDetails = false
                        )
                    } else currentState
                }
            }.onFailure { result ->
                _state.update { currentState ->
                    if (currentState is CardEditState.Content) {
                        currentState.copy(
                            isFetchingDetails = false
                        )
                    } else currentState
                }
                _errorMessage.update { "Couldn't get a transcription and an example. Error: " + result.message }
            }
        }
    }

    fun onSaveClick(): Boolean {
        val currentState = _state.value
        if (currentState !is CardEditState.Content) return false

        val errors = validateFields(currentState)

        if (errors.isNotEmpty()) {
            _state.update {
                if (it is CardEditState.Content) {
                    it.copy(errors = errors)
                } else it
            }
            return false
        }

        viewModelScope.launch {
            try {
                val card = createCardFromState(currentState)

                if (currentState.isEditing) {
                    cardRepository.updateCard(card)
                } else {
                    cardRepository.createCard(card)
                }

                _state.update { CardEditState.Saved }
            } catch (e: Exception) {
                _errorMessage.update { "Failed to save card: ${e.message}" }
            }
        }

        return true
    }

    private fun validateFields(state: CardEditState.Content): Map<ValidationErrorField, String> {
        val errors = mutableMapOf<ValidationErrorField, String>()

        if (state.word.isBlank()) {
            errors[ValidationErrorField.WORD] = "Word is required"
        }

        if (state.translation.isBlank()) {
            errors[ValidationErrorField.TRANSLATION] = "Translation is required"
        }

        return errors
    }

    private fun createCardFromState(state: CardEditState.Content): Card {
        val now = Clock.System.now()

        return Card(
            id = if (state.isEditing) state.cardId else 0,
            deckId = state.deckId,
            word = state.word.trim(),
            translation = state.translation.trim(),
            example = state.example.trim().takeIf { it.isNotBlank() },
            transcription = state.transcription.trim().takeIf { it.isNotBlank() },
            easinessFactor = state.originalCard?.easinessFactor ?: 2.5,
            interval = state.originalCard?.interval ?: 0,
            repetitions = state.originalCard?.repetitions ?: 0,
            nextReviewDate = state.originalCard?.nextReviewDate,
            createdAt = state.originalCard?.createdAt ?: now,
            updatedAt = now
        )
    }

    fun resetState() {
        _state.update { currentState ->
            if (currentState is CardEditState.Content) {
                currentState.copy(
                    word = currentState.originalCard?.word ?: "",
                    translation = currentState.originalCard?.translation ?: "",
                    example = currentState.originalCard?.example ?: "",
                    transcription = currentState.originalCard?.transcription ?: "",
                    errors = emptyMap(),
                    isFetchingDetails = false
                )
            } else currentState
        }
        debounceJob?.cancel()
    }

    fun clearErrorMessage() {
        _errorMessage.update { null }
    }
}