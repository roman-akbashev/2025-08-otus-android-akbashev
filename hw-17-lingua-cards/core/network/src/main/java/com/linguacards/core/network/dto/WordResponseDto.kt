package com.linguacards.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class WordResponseDto(
    val word: String,
    val phonetic: String? = null,
    val meanings: List<MeaningDto> = emptyList()
)

@Serializable
data class MeaningDto(
    val definitions: List<DefinitionDto> = emptyList()
)

@Serializable
data class DefinitionDto(
    val definition: String,
    // значение по умолчанию, потому что поле может отсутствовать
    val example: String? = null
)