package ru.otus.notesapp.domain.model

import java.util.Date

data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)