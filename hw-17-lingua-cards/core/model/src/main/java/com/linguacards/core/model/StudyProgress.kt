package com.linguacards.core.model

import kotlinx.serialization.Serializable

@Serializable
data class StudyProgress(
    val totalCards: Int,
    val studiedToday: Int,
    val newCards: Int,
    val reviewCards: Int
)