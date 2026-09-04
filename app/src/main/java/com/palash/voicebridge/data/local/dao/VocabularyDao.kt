package com.palash.voicebridge.data.local.dao

import androidx.room.*
import com.palash.voicebridge.data.local.entity.VocabularyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary ORDER BY category")
    fun getAllVocabulary(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE category = :category ORDER BY id")
    fun getVocabularyByCategory(category: String): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE id = :id")
    suspend fun getVocabularyById(id: String): VocabularyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vocabulary: List<VocabularyEntity>)

    @Query("SELECT COUNT(*) FROM vocabulary")
    suspend fun count(): Int
}
