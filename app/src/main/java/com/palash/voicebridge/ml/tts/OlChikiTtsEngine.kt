package com.palash.voicebridge.ml.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.palash.voicebridge.domain.engine.TtsEngine
import com.palash.voicebridge.domain.model.AudioResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * Replaced the old "sine wave" TTS with Android's native TextToSpeech.
 * Since Android doesn't have a Santali TTS, we transliterate the Ol Chiki 
 * back to Devanagari and use the Hindi voice to read it aloud.
 * This provides natural human-sounding dictation instead of robotic beeps.
 */
class OlChikiTtsEngine(
    private val context: Context
) : TtsEngine, TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val prefs = context.getSharedPreferences("tts_prefs", Context.MODE_PRIVATE)

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val locale = Locale("hi", "IN")
            val result = tts?.setLanguage(locale)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true
                reloadSettings()
            }
        }
    }

    fun reloadSettings() {
        val speed = prefs.getFloat("tts_speed", 0.95f)
        val pitch = prefs.getFloat("tts_pitch", 1.1f)
        val voiceName = prefs.getString("tts_voice_name", null)

        tts?.setSpeechRate(speed)
        tts?.setPitch(pitch)

        if (voiceName != null) {
            val voices = try { tts?.voices } catch (e: Exception) { null }
            val selectedVoice = voices?.find { it.name == voiceName }
            if (selectedVoice != null) {
                tts?.voice = selectedVoice
            }
        }
    }

    fun getAvailableVoices(): List<Voice> {
        return try {
            val allVoices = tts?.voices ?: return emptyList()
            // Filter for Hindi voices to ensure we pick appropriate ones
            allVoices.filter { it.locale.language == "hi" }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Maps Ol Chiki characters to Devanagari (Hindi) for pronunciation
    private val olChikiToDevanagariMap = mapOf(
        'ᱚ' to "अ", 'ᱟ' to "आ", 'ᱤ' to "इ", 'ᱩ' to "उ", 'ᱮ' to "ए", 'ᱳ' to "ओ",
        'ᱛ' to "त", 'ᱜ' to "ग", 'ᱝ' to "ंग", 'ᱞ' to "ल", 'ᱠ' to "क", 'ᱡ' to "ज",
        'ᱢ' to "म", 'ᱣ' to "व", 'ᱥ' to "स", 'ᱦ' to "ह", 'ᱨ' to "र", 'ᱵ' to "ब",
        'ᱪ' to "च", 'ᱫ' to "द", 'ᱬ' to "ण", 'ᱯ' to "प", 'ᱰ' to "ड", 'ᱱ' to "न",
        'ᱲ' to "ड़", 'ᱭ' to "य",
        '᱐' to "0", '᱑' to "1", '᱒' to "2", '᱓' to "3", '᱔' to "4", '᱕' to "5",
        '᱖' to "6", '᱗' to "7", '᱘' to "8", '᱙' to "9",
        '᱾' to "।", ' ' to " "
    )

    private fun transliterateToDevanagari(olChikiText: String): String {
        val builder = java.lang.StringBuilder()
        for (char in olChikiText) {
            builder.append(olChikiToDevanagariMap[char] ?: char)
        }
        return builder.toString()
    }

    override suspend fun synthesize(
        text: String,
        language: String
    ): AudioResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        if (!isInitialized || tts == null) {
            return@withContext AudioResult.TextOnly(text, "TTS Engine not initialized")
        }
        
        // Ensure settings are fresh in case they changed in Settings
        reloadSettings()

        // Transliterate Ol Chiki to Hindi so the Android TTS can read it
        val pronouncableText = transliterateToDevanagari(text)

        val outputFile = File(context.cacheDir, "tts_output.wav")
        if (outputFile.exists()) {
            outputFile.delete()
        }

        val utteranceId = "tts_${System.currentTimeMillis()}"
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        // Request synthesis to file
        val result = tts?.synthesizeToFile(pronouncableText, params, outputFile, utteranceId)

        if (result == TextToSpeech.SUCCESS) {
            // Wait for file to be written (up to 3 seconds)
            var attempts = 0
            while (!outputFile.exists() || outputFile.length() == 0L) {
                delay(100)
                attempts++
                if (attempts > 30) {
                    return@withContext AudioResult.TextOnly(text, "TTS synthesis timed out")
                }
            }
            
            // Allow a tiny bit more time for the file buffer to finish writing
            delay(150)
            
            val audioBytes = try {
                outputFile.readBytes()
            } catch (e: Exception) {
                return@withContext AudioResult.TextOnly(text, "Failed to read generated audio")
            }

            val actualSampleRate = getWavSampleRate(audioBytes)

            val latency = System.currentTimeMillis() - startTime
            return@withContext AudioResult.AudioData(
                audioBytes = audioBytes,
                sampleRate = actualSampleRate, // Approximate standard for Android TTS WAV
                latencyMs = latency,
                source = "Android-Native-TTS-Transliterated"
            )
        } else {
            return@withContext AudioResult.TextOnly(text, "Failed to synthesize speech")
        }
    }

    override fun isAvailable(): Boolean = isInitialized

    override fun engineName(): String = "Native Android TTS (Transliterated)"

    override fun supportedLanguages(): List<String> = listOf("sat_Olck", "hin_Deva")

    override fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    private fun getWavSampleRate(header: ByteArray): Int {
        if (header.size < 28) return 16000 // Fallback to 16kHz
        return (header[24].toInt() and 0xff) or
               ((header[25].toInt() and 0xff) shl 8) or
               ((header[26].toInt() and 0xff) shl 16) or
               ((header[27].toInt() and 0xff) shl 24)
    }
}
