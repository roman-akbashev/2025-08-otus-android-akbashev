package com.linguacards.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class WordResponseDto(
    val word: String,
    val phonetic: String?,
    val phonetics: List<PhoneticDto>,
    val meanings: List<MeaningDto>
)

@Serializable
data class PhoneticDto(
    val text: String?,
    val audio: String?
)

@Serializable
data class MeaningDto(
    val partOfSpeech: String,
    val definitions: List<DefinitionDto>
)

@Serializable
data class DefinitionDto(
    val definition: String,
    val example: String?,
    val synonyms: List<String>,
    val antonyms: List<String>
)