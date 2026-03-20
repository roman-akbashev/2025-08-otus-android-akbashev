package com.linguacards.core.model

import kotlinx.serialization.Serializable

@Serializable
data class WordDetails(
    val word: String,
    val transcription: String?,
    val example: String?,
    val definition: String?
)