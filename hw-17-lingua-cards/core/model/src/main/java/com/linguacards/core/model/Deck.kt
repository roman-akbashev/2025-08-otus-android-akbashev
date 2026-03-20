package com.linguacards.core.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Deck(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val cardCount: Int = 0
)