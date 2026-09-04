package com.palash.voicebridge.data.local.dao

import androidx.room.*
import com.palash.voicebridge.data.local.entity.FlashcardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY category")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE category = :category ORDER BY id")
    fun getFlashcardsByCategory(category: String): Flow<List<FlashcardEntity>>

    @Query("SELECT DISTINCT category FROM flashcards ORDER BY category")
    fun getCategories(): Flow<List<String>>

    @Query("SELECT * FROM flashcards WHERE id = :id")
    suspend fun getFlashcardById(id: String): FlashcardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flashcards: List<FlashcardEntity>)

    @Query("SELECT COUNT(*) FROM flashcards")
    suspend fun count(): Int
}
