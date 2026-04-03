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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
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
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadDecks()
    }

    private fun loadDecks() {
        combine(
            deckRepository.getAllDecks(),
            _searchQuery.debounce(300)
        ) { decks, searchQuery ->
            processDataUpdate(decks, searchQuery)
        }
            .catch { exception ->
                _errorMessage.update { exception.message ?: "Unknown error" }
                _state.update { DecksState.Empty }
            }
            .launchIn(viewModelScope)
    }

    private fun processDataUpdate(decks: List<Deck>, searchQuery: String) {
        val filteredDecks = filterDecksByQuery(decks, searchQuery)

        val newState = when {
            decks.isEmpty() && searchQuery.isBlank() -> {
                DecksState.Empty
            }

            decks.isNotEmpty() && filteredDecks.isEmpty() && searchQuery.isNotBlank() -> {
                DecksState.Success(
                    decks = emptyList(),
                    searchQuery = searchQuery
                )
            }

            else -> {
                DecksState.Success(
                    decks = filteredDecks,
                    searchQuery = searchQuery
                )
            }
        }
        _state.update { newState }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.update { query }
    }

    private fun filterDecksByQuery(decks: List<Deck>, query: String): List<Deck> {
        if (query.isBlank()) return decks

        return decks.filter { deck ->
            deck.name.contains(query, ignoreCase = true) ||
                    deck.description?.contains(query, ignoreCase = true) == true
        }
    }

    fun createDeck(name: String, description: String?) {
        viewModelScope.launch {
            try {
                deckRepository.createDeck(name, description)
            } catch (e: Exception) {
                _errorMessage.update { "Failed to create deck: ${e.message}" }
            }
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch {
            try {
                deckRepository.deleteDeck(deck.id)
            } catch (e: Exception) {
                _errorMessage.update { "Failed to delete deck: ${e.message}" }
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.update { null }
    }
}