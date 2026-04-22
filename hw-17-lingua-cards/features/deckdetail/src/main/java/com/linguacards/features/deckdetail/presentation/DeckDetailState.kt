package com.linguacards.features.deckdetail.presentation

import com.linguacards.core.model.Card
import com.linguacards.core.model.Deck

sealed class DeckDetailState {
    object Loading : DeckDetailState()
    data class Success(
        val deck: Deck,
        val cards: List<Card>,
        val searchQuery: String = ""
    ) : DeckDetailState()
    object Empty : DeckDetailState()
}