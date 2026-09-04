package com.palash.voicebridge.data.repository

import com.palash.voicebridge.data.local.dao.PhraseDao
import com.palash.voicebridge.domain.engine.TranslationEngine
import com.palash.voicebridge.domain.model.TranslationResult

/**
 * TranslationRepository implements the verified-phrase-first strategy:
 *   1. Normalize input text
 *   2. Look up in verified phrase database
 *   3. If found → return verified translation
 *   4. If not found → delegate to TranslationEngine (IndicTrans2 in Phase 2)
 */
class TranslationRepository(
    private val phraseDao: PhraseDao,
    private val translationEngine: TranslationEngine
) {
    suspend fun translate(
        text: String,
        sourceLanguage: String = "hin_Deva",
        targetLanguage: String = "sat_Olck"
    ): TranslationResult {
        val startTime = System.currentTimeMillis()

        // Step 1: Normalize input
        val normalized = normalizeHindi(text)

        // Step 2: Check verified phrase database
        val verifiedPhrase = phraseDao.findByNormalizedHindi(normalized)
        if (verifiedPhrase != null) {
            val latency = System.currentTimeMillis() - startTime
            return TranslationResult(
                sourceText = text,
                translatedText = verifiedPhrase.santali,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
                isVerified = true,
                latencyMs = latency,
                engineUsed = "verified_db"
            )
        }

        // Step 3: Delegate to translation engine
        return translationEngine.translate(text, sourceLanguage, targetLanguage)
    }

    private fun normalizeHindi(text: String): String {
        return text.trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
            .replace("।", "।") // normalize Devanagari danda
    }
}
