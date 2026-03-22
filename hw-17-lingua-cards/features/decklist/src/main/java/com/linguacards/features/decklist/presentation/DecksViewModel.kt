package com.linguacards.features.decklist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linguacards.core.domain.repository.DeckRepository
import com.linguacards.core.model.Deck
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DecksViewModel @Inject constructor(
    private val deckRepository: DeckRepository
) : ViewModel() {

    private val _state = MutableStateFlow<DecksState>(DecksState.Loading)
    val state: StateFlow<DecksState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var allDecks: List<Deck> = emptyList()

    init {
        loadDecks()
    }

    fun loadDecks() {
        deckRepository.getAllDecks()
            .onStart { _state.value = DecksState.Loading }
            .onEach { decks ->
                allDecks = decks
                filterDecks(_searchQuery.value)
            }
            .catch { exception ->
                _state.value = DecksState.Error(exception.message ?: "Unknown error")
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        filterDecks(query)
    }

    private fun filterDecks(query: String) {
        val filtered = if (query.isBlank()) {
            allDecks
        } else {
            allDecks.filter { deck ->
                deck.name.contains(query, ignoreCase = true) ||
                        deck.description?.contains(query, ignoreCase = true) == true
            }
        }
        _state.value = DecksState.Success(filtered)
    }

    fun createDeck(name: String, description: String?) {
        viewModelScope.launch {
            deckRepository.createDeck(name, description)
            // Список обновится автоматически через Flow
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch {
            deckRepository.deleteDeck(deck.id)
            // Список обновится автоматически
        }
    }
}