package com.palash.voicebridge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val classLevel: Int,
    val subject: String,
    val subjectHindi: String,
    val topic: String,
    val topicHindi: String,
    val title: String,
    val titleHindi: String,
    val titleSantali: String,
    val learningOutcome: String,
    val learningOutcomeHindi: String,
    val instructionHindi: String,
    val instructionSantali: String,
    val activity: String,
    val activityHindi: String,
    val assessmentHindi: String,
    val assessmentSantali: String,
    val teacherPromptHindi: String,
    val teacherPromptSantali: String,
    val audioFile: String? = null
)
