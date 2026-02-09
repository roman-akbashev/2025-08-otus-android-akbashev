package ru.otus.notesapp.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.otus.notesapp.data.local.NoteDao
import ru.otus.notesapp.data.local.NoteEntity
import ru.otus.notesapp.domain.model.Note
import ru.otus.notesapp.domain.repository.NoteRepository
import java.util.Date
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getNoteById(id: Long): Note? {
        return noteDao.getNoteById(id)?.toDomain()
    }

    override suspend fun insertNote(note: Note): Long {
        val now = Date()
        val entity = NoteEntity(
            title = note.title,
            content = note.content,
            createdAt = note.createdAt ?: now,
            updatedAt = now
        )
        return noteDao.insertNote(entity)
    }

    override suspend fun updateNote(note: Note) {
        val entity = noteDao.getNoteById(note.id) ?: return
        val updatedEntity = entity.copy(
            title = note.title,
            content = note.content,
            updatedAt = Date()
        )
        noteDao.updateNote(updatedEntity)
    }

    override suspend fun deleteNote(note: Note) {
        val entity = noteDao.getNoteById(note.id) ?: return
        noteDao.deleteNote(entity)
    }

    private fun NoteEntity.toDomain(): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}