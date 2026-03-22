package com.linguacards.features.cardedit.presentation

import com.linguacards.core.model.Card

sealed class CardEditState {
    object Loading : CardEditState()
    data class Content(
        val deckId: Long,
        val cardId: Long,
        val word: String = "",
        val translation: String = "",
        val example: String = "",
        val transcription: String = "",
        val isEditing: Boolean = false,
        val isFetchingDetails: Boolean = false,
        val errors: Map<String, String> = emptyMap(),
        val originalCard: Card? = null
    ) : CardEditState()

    data class Error(val message: String) : CardEditState()
    object Saved : CardEditState() // Для сигнала об успешном сохранении
}