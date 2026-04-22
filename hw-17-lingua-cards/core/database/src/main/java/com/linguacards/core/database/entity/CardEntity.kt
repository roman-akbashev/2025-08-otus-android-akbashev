package com.linguacards.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deckId: Long,
    val word: String,
    val translation: String,
    val example: String?,
    val transcription: String?,
    val easinessFactor: Double,
    val interval: Int,
    val repetitions: Int,
    val nextReviewDate: Long?,
    val createdAt: Long,
    val updatedAt: Long
)