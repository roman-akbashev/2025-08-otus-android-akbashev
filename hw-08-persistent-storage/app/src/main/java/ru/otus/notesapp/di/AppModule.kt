package ru.otus.notesapp.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.otus.notesapp.data.local.NoteDao
import ru.otus.notesapp.data.local.NotesDatabase
import ru.otus.notesapp.data.repository.NoteRepositoryImpl
import ru.otus.notesapp.domain.repository.NoteRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNotesDatabase(@ApplicationContext context: Context): NotesDatabase {
        return NotesDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: NotesDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideNoteRepository(noteDao: NoteDao): NoteRepository {
        return NoteRepositoryImpl(noteDao)
    }
}