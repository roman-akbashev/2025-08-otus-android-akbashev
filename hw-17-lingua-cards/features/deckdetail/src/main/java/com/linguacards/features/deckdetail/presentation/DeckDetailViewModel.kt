package com.linguacards.features.deckdetail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguacards.core.domain.repository.CardRepository
import com.linguacards.core.domain.repository.DeckRepository
import com.linguacards.core.model.Card
import com.linguacards.core.model.Deck
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeckDetailViewModel @Inject constructor(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val deckId: Long = savedStateHandle.get<Long>("deckId") ?: 0L
    private val _state = MutableStateFlow<DeckDetailState>(DeckDetailState.Loading)
    val state: StateFlow<DeckDetailState> = _state.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadDeckData()
    }

    @OptIn(FlowPreview::class)
    private fun loadDeckData() {
        combine(
            deckRepository.getDeckById(deckId),
            cardRepository.getCardsByDeckId(deckId),
            _searchQuery.debounce(300)
        ) { deck, cards, searchQuery ->
            processDataUpdate(deck, cards, searchQuery)
        }
            .catch { exception ->
                _errorMessage.update { exception.message ?: "Unknown error" }
                _state.update { DeckDetailState.Empty }
            }
            .launchIn(viewModelScope)
    }

    private fun processDataUpdate(deck: Deck?, cards: List<Card>, searchQuery: String) {
        val filteredCards = filterCardsByQuery(cards, searchQuery)

        when {
            deck == null -> {
                _errorMessage.update { "Deck not found" }
                _state.update { DeckDetailState.Empty }
            }

            cards.isEmpty() && searchQuery.isBlank() -> {
                _state.value = DeckDetailState.Empty
            }

            cards.isNotEmpty() && filteredCards.isEmpty() && searchQuery.isNotBlank() -> {
                _state.value = DeckDetailState.Success(
                    deck = deck,
                    cards = emptyList(),
                    searchQuery = searchQuery
                )
            }

            else -> {
                _state.value = DeckDetailState.Success(
                    deck = deck,
                    cards = filteredCards,
                    searchQuery = searchQuery
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.update { query }
    }

    private fun filterCardsByQuery(cards: List<Card>, query: String): List<Card> {
        if (query.isBlank()) return cards

        return cards.filter { card ->
            card.word.contains(query, ignoreCase = true) ||
                    card.translation.contains(query, ignoreCase = true) ||
                    (card.example?.contains(query, ignoreCase = true) == true)
        }
    }

    fun deleteCard(card: Card) {
        viewModelScope.launch {
            try {
                cardRepository.deleteCard(card.id)
            } catch (e: Exception) {
                _errorMessage.update { "Failed to delete card: ${e.message}" }
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.update { null }
    }
}