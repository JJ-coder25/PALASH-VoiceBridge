package com.palash.voicebridge.data.local.dao

import androidx.room.*
import com.palash.voicebridge.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY classLevel, subject, topic")
    fun getAllLessons(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE classLevel = :classLevel ORDER BY subject, topic")
    fun getLessonsByClass(classLevel: Int): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE classLevel = :classLevel AND subject = :subject ORDER BY topic")
    fun getLessonsByClassAndSubject(classLevel: Int, subject: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :id")
    suspend fun getLessonById(id: String): LessonEntity?

    @Query("SELECT DISTINCT classLevel FROM lessons ORDER BY classLevel")
    fun getClassLevels(): Flow<List<Int>>

    @Query("SELECT DISTINCT subject FROM lessons WHERE classLevel = :classLevel ORDER BY subject")
    fun getSubjects(classLevel: Int): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lessons: List<LessonEntity>)

    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun count(): Int
}
