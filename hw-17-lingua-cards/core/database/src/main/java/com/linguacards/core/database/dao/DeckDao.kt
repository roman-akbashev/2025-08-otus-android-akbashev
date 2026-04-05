package com.linguacards.core.database.dao

import androidx.room.*
import com.linguacards.core.database.entity.DeckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks ORDER BY updatedAt DESC")
    fun getAllDecks(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE id = :deckId")
    suspend fun getDeckById(deckId: Long): DeckEntity?

    @Query("SELECT * FROM decks WHERE id = :deckId")
    fun getDeckByIdFlow(deckId: Long): Flow<DeckEntity?>

    @Insert
    suspend fun insertDeck(deck: DeckEntity): Long

    @Update
    suspend fun updateDeck(deck: DeckEntity)

    @Delete
    suspend fun deleteDeck(deck: DeckEntity)

    @Query("DELETE FROM decks")
    suspend fun deleteAllDecks()
}