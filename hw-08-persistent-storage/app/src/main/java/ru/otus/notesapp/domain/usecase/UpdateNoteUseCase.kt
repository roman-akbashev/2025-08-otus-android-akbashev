package ru.otus.notesapp.domain.usecase

import ru.otus.notesapp.domain.model.Note
import ru.otus.notesapp.domain.repository.NoteRepository
import javax.inject.Inject

class UpdateNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.updateNote(note)
    }
}