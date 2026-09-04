package com.palash.voicebridge.data.content

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.palash.voicebridge.data.local.AppDatabase
import com.palash.voicebridge.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads JSON content from assets/ into the Room database on first launch.
 */
class ContentLoader(
    private val context: Context,
    private val database: AppDatabase
) {
    private val gson = Gson()

    suspend fun loadContentIfNeeded() = withContext(Dispatchers.IO) {
        // Only load if DB is empty
        if (database.lessonDao().count() > 0) return@withContext

        loadLessons()
        loadFlashcards()
        loadVocabulary()
        loadVerifiedPhrases()
    }

    private suspend fun loadLessons() {
        val json = readAsset("content/lessons.json")
        val type = object : TypeToken<List<LessonJson>>() {}.type
        val lessons: List<LessonJson> = gson.fromJson(json, type)

        database.lessonDao().insertAll(lessons.map { it.toEntity() })
    }

    private suspend fun loadFlashcards() {
        val json = readAsset("content/flashcards.json")
        val type = object : TypeToken<List<FlashcardJson>>() {}.type
        val flashcards: List<FlashcardJson> = gson.fromJson(json, type)

        database.flashcardDao().insertAll(flashcards.map { it.toEntity() })
    }

    private suspend fun loadVocabulary() {
        val json = readAsset("content/vocabulary.json")
        val type = object : TypeToken<List<VocabularyJson>>() {}.type
        val vocabulary: List<VocabularyJson> = gson.fromJson(json, type)

        database.vocabularyDao().insertAll(vocabulary.map { it.toEntity() })
    }

    private suspend fun loadVerifiedPhrases() {
        val json = readAsset("content/verified_phrases.json")
        val type = object : TypeToken<List<PhraseJson>>() {}.type
        val phrases: List<PhraseJson> = gson.fromJson(json, type)

        database.phraseDao().insertAll(phrases.map { it.toEntity() })
    }

    private fun readAsset(filename: String): String {
        return context.assets.open(filename).bufferedReader().use { it.readText() }
    }

    // --- JSON data classes for parsing ---

    private data class LessonJson(
        val id: String,
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
    ) {
        fun toEntity() = LessonEntity(
            id, classLevel, subject, subjectHindi, topic, topicHindi,
            title, titleHindi, titleSantali, learningOutcome, learningOutcomeHindi,
            instructionHindi, instructionSantali, activity, activityHindi,
            assessmentHindi, assessmentSantali, teacherPromptHindi, teacherPromptSantali,
            audioFile
        )
    }

    private data class FlashcardJson(
        val id: String,
        val category: String,
        val categoryHindi: String,
        val hindi: String,
        val santali: String,
        val english: String,
        val emoji: String,
        val audioFile: String? = null
    ) {
        fun toEntity() = FlashcardEntity(
            id, category, categoryHindi, hindi, santali, english, emoji, audioFile
        )
    }

    private data class VocabularyJson(
        val id: String,
        val category: String,
        val hindi: String,
        val santali: String,
        val english: String,
        val emoji: String? = null,
        val audioFile: String? = null,
        val verified: Boolean = false
    ) {
        fun toEntity() = VocabularyEntity(
            id, category, hindi, santali, english, emoji, audioFile, verified
        )
    }

    private data class PhraseJson(
        val id: String,
        val hindi: String,
        val santali: String,
        val category: String,
        val hindi_normalized: String,
        val audio_file: String? = null,
        val verified: Boolean = true
    ) {
        fun toEntity() = VerifiedPhraseEntity(
            id, hindi, santali, category,
            hindi_normalized.trim().lowercase(),
            audio_file, verified
        )
    }
}
