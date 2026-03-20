package com.linguacards.core.network.mapper

import com.linguacards.core.model.WordDetails
import com.linguacards.core.network.dto.WordResponseDto

object WordDetailsMapper {

    fun mapToDomain(dto: WordResponseDto): WordDetails {
        // Находим первую транскрипцию
        val transcription = dto.phonetics
            .firstOrNull { !it.text.isNullOrBlank() }
            ?.text
            ?.replace("/", "")

        // Находим первый пример использования
        val example = dto.meanings
            .firstOrNull()
            ?.definitions
            ?.firstOrNull { !it.example.isNullOrBlank() }
            ?.example

        // Находим первое определение
        val definition = dto.meanings
            .firstOrNull()
            ?.definitions
            ?.firstOrNull()
            ?.definition

        return WordDetails(
            word = dto.word,
            transcription = transcription,
            example = example,
            definition = definition
        )
    }
}