package com.palash.voicebridge.data.repository

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.palash.voicebridge.domain.engine.TtsEngine
import com.palash.voicebridge.domain.model.AudioResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AudioRepository manages audio generation and playback for Santali translations.
 *
 * Implements 3-tier fallback architecture:
 * 1. Cached verified native audio clip (if audioFileName provided)
 * 2. Local offline Ol Chiki TTS synthesis (waveform generation)
 * 3. Text-only fallback (never crashes the UI)
 */
class AudioRepository(
    private val context: Context,
    private val ttsEngine: TtsEngine
) {
    private var currentAudioTrack: AudioTrack? = null

    /**
     * Obtains audio data via the 3-tier fallback pipeline.
     */
    suspend fun getAudio(
        text: String,
        language: String = "sat_Olck",
        audioFileName: String? = null
    ): AudioResult = withContext(Dispatchers.IO) {
        // Tier 1: Check cached audio
        if (audioFileName != null) {
            val cachedAudio = loadCachedAudio(audioFileName)
            if (cachedAudio != null) return@withContext cachedAudio
        }

        // Tier 2: Local Santali TTS synthesis
        if (ttsEngine.isAvailable()) {
            try {
                val result = ttsEngine.synthesize(text, language)
                if (result is AudioResult.AudioData) {
                    return@withContext result
                }
            } catch (e: Exception) {
                // Fall through to Tier 3
            }
        }

        // Tier 3: Text-only fallback
        AudioResult.TextOnly(
            text = text,
            reason = "Offline text fallback. Audio synthesizer unavailable."
        )
    }

    /**
     * Plays the audio data through the Android device speaker using AudioTrack.
     */
    suspend fun playAudio(audioResult: AudioResult): Boolean = withContext(Dispatchers.IO) {
        if (audioResult !is AudioResult.AudioData) return@withContext false
        return@withContext playAudioBytes(audioResult.audioBytes, audioResult.sampleRate)
    }

    /**
     * Plays raw PCM 16-bit mono audio bytes.
     */
    suspend fun playAudioBytes(pcmBytes: ByteArray, sampleRate: Int = 16000): Boolean = withContext(Dispatchers.IO) {
        if (pcmBytes.isEmpty()) return@withContext false

        stopAudio()

        try {
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(pcmBytes.size)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcmBytes, 0, pcmBytes.size)
            track.play()

            currentAudioTrack = track
            return@withContext true
        } catch (e: Exception) {
            return@withContext false
        }
    }

    fun stopAudio() {
        try {
            currentAudioTrack?.stop()
            currentAudioTrack?.release()
        } catch (e: Exception) {
            // Ignore
        } finally {
            currentAudioTrack = null
        }
    }

    private fun loadCachedAudio(filename: String): AudioResult.AudioData? {
        return try {
            val bytes = context.assets.open("content/audio/$filename").readBytes()
            AudioResult.AudioData(
                audioBytes = bytes,
                sampleRate = 16000,
                latencyMs = 0,
                source = "cached"
            )
        } catch (e: Exception) {
            null
        }
    }
}
