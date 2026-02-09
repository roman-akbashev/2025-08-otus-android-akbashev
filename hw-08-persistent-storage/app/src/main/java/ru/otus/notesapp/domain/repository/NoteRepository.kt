package ru.otus.notesapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.otus.notesapp.domain.model.Note

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?
    suspend fun insertNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
}