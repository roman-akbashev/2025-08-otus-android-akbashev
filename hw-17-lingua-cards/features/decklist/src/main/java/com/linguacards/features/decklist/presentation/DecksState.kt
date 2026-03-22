package com.linguacards.features.decklist.presentation

import com.linguacards.core.model.Deck

sealed class DecksState {
    object Loading : DecksState()
    data class Success(val decks: List<Deck>) : DecksState()
    data class Error(val message: String) : DecksState()
}