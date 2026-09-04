package com.palash.voicebridge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary")
data class VocabularyEntity(
    @PrimaryKey val id: String,
    val category: String,
    val hindi: String,
    val santali: String,
    val english: String,
    val emoji: String? = null,
    val audioFile: String? = null,
    val verified: Boolean = false
)
