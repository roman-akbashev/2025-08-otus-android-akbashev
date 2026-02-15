package ru.otus.notesapp.domain.usecase

import ru.otus.notesapp.domain.model.Note
import ru.otus.notesapp.domain.repository.NoteRepository
import javax.inject.Inject

class AddNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Long {
        return repository.insertNote(note)
    }
}