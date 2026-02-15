package ru.otus.notesapp.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.otus.notesapp.domain.model.Note
import ru.otus.notesapp.domain.usecase.AddNoteUseCase
import ru.otus.notesapp.domain.usecase.DeleteNoteUseCase
import ru.otus.notesapp.domain.usecase.GetNotesUseCase
import ru.otus.notesapp.domain.usecase.UpdateNoteUseCase
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            getNotesUseCase().collectLatest { notesList ->
                _notes.value = notesList
            }
        }
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            val note = Note(title = title, content = content)
            addNoteUseCase(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            updateNoteUseCase(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            deleteNoteUseCase(note)
        }
    }

    fun showAddNoteDialog() {
        _uiState.value = _uiState.value.copy(
            isAddNoteDialogVisible = true
        )
    }

    fun hideAddNoteDialog() {
        _uiState.value = _uiState.value.copy(
            isAddNoteDialogVisible = false
        )
    }
}

data class NotesUiState(
    val isAddNoteDialogVisible: Boolean = false,
    val isLoading: Boolean = false
)