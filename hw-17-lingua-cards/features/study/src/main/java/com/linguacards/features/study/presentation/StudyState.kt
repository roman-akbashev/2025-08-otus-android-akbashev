package com.linguacards.features.study.presentation

sealed class StudyState {
    object Loading : StudyState()
    object Finished : StudyState()
    data class Card(
        val card: com.linguacards.core.model.Card,
        val isFlipped: Boolean,
        val progress: String,
        val isProcessing: Boolean = false
    ) : StudyState()
}