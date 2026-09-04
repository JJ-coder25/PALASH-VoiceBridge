package com.palash.voicebridge.data.local.dao

import androidx.room.*
import com.palash.voicebridge.data.local.entity.VerifiedPhraseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhraseDao {
    @Query("SELECT * FROM verified_phrases ORDER BY category")
    fun getAllPhrases(): Flow<List<VerifiedPhraseEntity>>

    @Query("SELECT * FROM verified_phrases WHERE hindiNormalized = :normalizedHindi LIMIT 1")
    suspend fun findByNormalizedHindi(normalizedHindi: String): VerifiedPhraseEntity?

    @Query("SELECT * FROM verified_phrases WHERE hindi LIKE '%' || :query || '%' LIMIT 5")
    suspend fun searchPhrases(query: String): List<VerifiedPhraseEntity>

    @Query("SELECT * FROM verified_phrases WHERE category = :category ORDER BY id")
    fun getPhrasesByCategory(category: String): Flow<List<VerifiedPhraseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(phrases: List<VerifiedPhraseEntity>)

    @Query("SELECT COUNT(*) FROM verified_phrases")
    suspend fun count(): Int
}
