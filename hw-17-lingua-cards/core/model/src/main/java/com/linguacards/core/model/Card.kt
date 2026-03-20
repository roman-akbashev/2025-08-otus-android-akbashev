package com.linguacards.core.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Card (
    val id: Long = 0,
    val deckId: Long,
    val word: String,
    val translation: String,
    val example: String? = null,
    val transcription: String? = null,
    // Поля для SM-2 алгоритма
    val easinessFactor: Double = 2.5,
    val interval: Int = 0,
    val repetitions: Int = 0,
    val nextReviewDate: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)