package com.palash.voicebridge.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.palash.voicebridge.data.local.dao.*
import com.palash.voicebridge.data.local.entity.*

@Database(
    entities = [
        LessonEntity::class,
        FlashcardEntity::class,
        VocabularyEntity::class,
        VerifiedPhraseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun phraseDao(): PhraseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "palash_voicebridge.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
