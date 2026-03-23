package com.linguacards.features.deckdetail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguacards.core.domain.repository.CardRepository
import com.linguacards.core.domain.repository.DeckRepository
import com.linguacards.core.model.Card
import com.linguacards.core.model.Deck
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
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

    init {
        loadDeckData()
    }

    private fun loadDeckData() {
        // Получаем потоки данных
        val deckFlow = deckRepository.getDeckById(deckId)
        val cardsFlow = cardRepository.getCardsByDeckId(deckId)

        // Комбинируем все потоки и обрабатываем изменения
        combine(
            deckFlow,
            cardsFlow,
            _searchQuery
        ) { deck, cards, searchQuery ->
            processDataUpdate(deck, cards, searchQuery)
        }
            .onStart {
                // Показываем Loading при старте
                _state.value = DeckDetailState.Loading
            }
            .catch { exception ->
                _state.value = DeckDetailState.Error(exception.message ?: "Unknown error")
            }
            .launchIn(viewModelScope)
    }

    private fun processDataUpdate(deck: Deck?, cards: List<Card>, searchQuery: String) {
        val filteredCards = filterCardsByQuery(cards, searchQuery)

        when {
            deck == null -> {
                _state.value = DeckDetailState.Error("Deck not found")
            }

            cards.isEmpty() && searchQuery.isBlank() -> {
                // Действительно пустая колода
                _state.value = DeckDetailState.Empty
            }

            cards.isNotEmpty() && filteredCards.isEmpty() && searchQuery.isNotBlank() -> {
                // Есть карточки, но поиск не дал результатов
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
        _searchQuery.value = query
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
                // Список обновится автоматически через Flow
            } catch (e: Exception) {
                _state.value = DeckDetailState.Error("Failed to delete card: ${e.message}")
            }
        }
    }

    fun refreshData() {
        // Просто вызываем обновление через _searchQuery
        _searchQuery.value = _searchQuery.value
    }
}