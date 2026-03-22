package com.linguacards.features.deckdetail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguacards.core.domain.repository.CardRepository
import com.linguacards.core.domain.repository.DeckRepository
import com.linguacards.core.model.Card
import com.linguacards.core.model.Deck
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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

    private var allCards: List<Card> = emptyList()
    private var currentDeck: Deck? = null

    init {
        loadDeckData()
    }

    private fun loadDeckData() {
        combine(
            deckRepository.getDeckByIdFlow(deckId),
            cardRepository.getCardsByDeckId(deckId)
        ) { deck, cards ->
            if (deck == null) {
                DeckDetailState.Error("Deck not found")
            } else {
                currentDeck = deck
                allCards = cards
                val filteredCards = filterCardsByQuery(cards, _searchQuery.value)

                if (filteredCards.isEmpty() && cards.isNotEmpty()) {
                    // Есть карточки, но ни одна не подходит под поиск
                    DeckDetailState.Success(
                        deck = deck,
                        cards = filteredCards,
                        searchQuery = _searchQuery.value
                    )
                } else if (filteredCards.isEmpty()) {
                    DeckDetailState.Empty
                } else {
                    DeckDetailState.Success(
                        deck = deck,
                        cards = filteredCards,
                        searchQuery = _searchQuery.value
                    )
                }
            }
        }
            .onStart { _state.value = DeckDetailState.Loading }
            .catch { exception ->
                _state.value = DeckDetailState.Error(exception.message ?: "Unknown error")
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        updateFilteredCards()
    }

    private fun updateFilteredCards() {
        val filtered = filterCardsByQuery(allCards, _searchQuery.value)

        if (currentDeck != null) {
            _state.value = when {
                filtered.isEmpty() && allCards.isNotEmpty() ->
                    DeckDetailState.Success(
                        deck = currentDeck!!,
                        cards = filtered,
                        searchQuery = _searchQuery.value
                    )

                filtered.isEmpty() ->
                    DeckDetailState.Empty

                else ->
                    DeckDetailState.Success(
                        deck = currentDeck!!,
                        cards = filtered,
                        searchQuery = _searchQuery.value
                    )
            }
        }
    }

    private fun filterCardsByQuery(cards: List<Card>, query: String): List<Card> {
        if (query.isBlank()) return cards

        return cards.filter { card ->
            card.word.contains(query, ignoreCase = true) ||
                    card.translation.contains(query, ignoreCase = true) ||
                    card.example?.contains(query, ignoreCase = true) == true
        }
    }

    fun deleteCard(card: Card) {
        viewModelScope.launch {
            try {
                cardRepository.deleteCard(card.id)
                // Список обновится автоматически через Flow
            } catch (e: Exception) {
                // Обработка ошибки
                _state.value = DeckDetailState.Error("Failed to delete card: ${e.message}")
            }
        }
    }

    fun refreshData() {
        loadDeckData()
    }
}

// Extension функция для получения Flow<Deck?> по id
fun DeckRepository.getDeckByIdFlow(deckId: Long): Flow<Deck?> {
    return getAllDecks().map { decks -> decks.find { it.id == deckId } }
}