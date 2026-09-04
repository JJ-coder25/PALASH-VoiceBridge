package com.palash.voicebridge.data.repository

import com.palash.voicebridge.data.local.dao.*
import com.palash.voicebridge.data.local.entity.*
import com.palash.voicebridge.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for all educational content (lessons, flashcards, vocabulary).
 */
class ContentRepository(
    private val lessonDao: LessonDao,
    private val flashcardDao: FlashcardDao,
    private val vocabularyDao: VocabularyDao
) {
    // --- Lessons ---

    fun getAllLessons(): Flow<List<Lesson>> = lessonDao.getAllLessons().map { list ->
        list.map { it.toDomain() }
    }

    fun getLessonsByClass(classLevel: Int): Flow<List<Lesson>> =
        lessonDao.getLessonsByClass(classLevel).map { list -> list.map { it.toDomain() } }

    fun getLessonsByClassAndSubject(classLevel: Int, subject: String): Flow<List<Lesson>> =
        lessonDao.getLessonsByClassAndSubject(classLevel, subject).map { list -> list.map { it.toDomain() } }

    suspend fun getLessonById(id: String): Lesson? = lessonDao.getLessonById(id)?.toDomain()

    fun getClassLevels(): Flow<List<Int>> = lessonDao.getClassLevels()

    fun getSubjects(classLevel: Int): Flow<List<String>> = lessonDao.getSubjects(classLevel)

    // --- Flashcards ---

    fun getAllFlashcards(): Flow<List<Flashcard>> = flashcardDao.getAllFlashcards().map { list ->
        list.map { it.toDomain() }
    }

    fun getFlashcardsByCategory(category: String): Flow<List<Flashcard>> =
        flashcardDao.getFlashcardsByCategory(category).map { list -> list.map { it.toDomain() } }

    fun getFlashcardCategories(): Flow<List<String>> = flashcardDao.getCategories()

    // --- Vocabulary ---

    fun getAllVocabulary(): Flow<List<VocabularyEntry>> = vocabularyDao.getAllVocabulary().map { list ->
        list.map { it.toDomain() }
    }

    fun getVocabularyByCategory(category: String): Flow<List<VocabularyEntry>> =
        vocabularyDao.getVocabularyByCategory(category).map { list -> list.map { it.toDomain() } }

    // --- Entity to Domain Mappers ---

    private fun LessonEntity.toDomain() = Lesson(
        id, classLevel, subject, subjectHindi, topic, topicHindi,
        title, titleHindi, titleSantali, learningOutcome, learningOutcomeHindi,
        instructionHindi, instructionSantali, activity, activityHindi,
        assessmentHindi, assessmentSantali, teacherPromptHindi, teacherPromptSantali,
        audioFile
    )

    private fun FlashcardEntity.toDomain() = Flashcard(
        id, category, categoryHindi, hindi, santali, english, emoji, audioFile
    )

    private fun VocabularyEntity.toDomain() = VocabularyEntry(
        id, category, hindi, santali, english, emoji, audioFile, verified
    )
}
