package com.palash.voicebridge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Verified phrase translations that are used as a reliable fallback
 * before neural translation. These are human-reviewed translations
 * for critical classroom phrases.
 */
@Entity(tableName = "verified_phrases")
data class VerifiedPhraseEntity(
    @PrimaryKey val id: String,
    val hindi: String,
    val santali: String,
    val category: String,       // e.g., "classroom", "math", "greeting"
    val hindiNormalized: String, // lowercase, trimmed for matching
    val audioFile: String? = null,
    val verified: Boolean = true
)
