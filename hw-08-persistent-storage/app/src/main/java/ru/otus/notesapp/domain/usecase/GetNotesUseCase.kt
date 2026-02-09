package ru.otus.notesapp.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.otus.notesapp.domain.model.Note
import ru.otus.notesapp.domain.repository.NoteRepository
import javax.inject.Inject

class GetNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<List<Note>> {
        return repository.getAllNotes()
    }
}