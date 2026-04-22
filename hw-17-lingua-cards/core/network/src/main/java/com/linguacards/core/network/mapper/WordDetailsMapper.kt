package com.linguacards.core.network.mapper

import com.linguacards.core.model.WordDetails
import com.linguacards.core.network.dto.WordResponseDto

object WordDetailsMapper {

    fun mapToDomain(dto: WordResponseDto): WordDetails {
        // Берем транскрипцию из phonetic и удаляем слэши
        val transcription = dto.phonetic
            ?.replace("/", "")
            ?.trim()

        // Находим первый пример использования
        val example = dto.meanings
            .asSequence()
            .flatMap { it.definitions.asSequence() }
            .firstOrNull { !it.example.isNullOrBlank() }
            ?.example

        return WordDetails(
            word = dto.word,
            transcription = transcription,
            example = example
        )
    }
}