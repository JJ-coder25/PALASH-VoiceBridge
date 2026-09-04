package com.palash.voicebridge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey val id: String,
    val category: String,
    val categoryHindi: String,
    val hindi: String,
    val santali: String,
    val english: String,
    val emoji: String,
    val audioFile: String? = null
)
