package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object NewsAudioEffects {
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Plays a dramatic TV Breaking News Stinger (brass-style fanfare tri-tone).
     */
    fun playBreakingNewsStinger() {
        scope.launch {
            try {
                // Chord progression: C5 (523Hz), E5 (659Hz), G5 (784Hz), C6 (1046Hz)
                val sampleRate = 44100
                val durationMs = 900
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)

                val chord = listOf(523.25, 659.25, 783.99, 1046.50)

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    // Envelope: fast attack, slight decay
                    val envelope = if (time < 0.05) time / 0.05 else (1.0 - (time - 0.05) / 0.85).coerceIn(0.0, 1.0)
                    
                    var sum = 0.0
                    for (freq in chord) {
                        sum += sin(2.0 * Math.PI * freq * time)
                    }
                    val sampleVal = (sum / chord.size) * envelope * Short.MAX_VALUE * 0.85
                    samples[i] = sampleVal.toInt().toShort()
                }

                playPcmBuffer(samples, sampleRate)
            } catch (e: Exception) {
                Log.e("NewsAudioEffects", "Error playing stinger", e)
                fallbackTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 500)
            }
        }
    }

    /**
     * Plays an urgent alert tone (for flash news / breaking news alerts).
     */
    fun playUrgentAlert() {
        scope.launch {
            try {
                val sampleRate = 44100
                val durationMs = 500
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    // Sirens / dual frequency frequency modulation
                    val freq = if ((time * 12).toInt() % 2 == 0) 880.0 else 1174.0
                    val envelope = (1.0 - (time / 0.5)).coerceIn(0.0, 1.0)
                    val sampleVal = sin(2.0 * Math.PI * freq * time) * envelope * Short.MAX_VALUE * 0.7
                    samples[i] = sampleVal.toInt().toShort()
                }

                playPcmBuffer(samples, sampleRate)
            } catch (e: Exception) {
                Log.e("NewsAudioEffects", "Error playing alert", e)
                fallbackTone(ToneGenerator.TONE_PROP_BEEP, 300)
            }
        }
    }

    /**
     * Plays a crisp news ticker update click/beep.
     */
    fun playTickerBeep() {
        scope.launch {
            try {
                val sampleRate = 44100
                val durationMs = 120
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    val freq = 1200.0
                    val envelope = (1.0 - (time / 0.12)).coerceIn(0.0, 1.0)
                    val sampleVal = sin(2.0 * Math.PI * freq * time) * envelope * Short.MAX_VALUE * 0.5
                    samples[i] = sampleVal.toInt().toShort()
                }

                playPcmBuffer(samples, sampleRate)
            } catch (e: Exception) {
                Log.e("NewsAudioEffects", "Error playing ticker beep", e)
            }
        }
    }

    /**
     * Plays an urgent breaking news alert tone (used for split / breaking triggers).
     */
    fun playBreakingAlert() {
        playUrgentAlert()
    }

    /**
     * Plays a clean studio chime when starting voiceover recording.
     */
    fun playMicChime() {
        scope.launch {
            try {
                val sampleRate = 44100
                val durationMs = 240
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    val freq = if (time < 0.12) 880.0 else 1318.5
                    val envelope = (1.0 - (time / 0.24)).coerceIn(0.0, 1.0)
                    val sampleVal = sin(2.0 * Math.PI * freq * time) * envelope * Short.MAX_VALUE * 0.6
                    samples[i] = sampleVal.toInt().toShort()
                }

                playPcmBuffer(samples, sampleRate)
            } catch (e: Exception) {
                Log.e("NewsAudioEffects", "Error playing mic chime", e)
            }
        }
    }

    /**
     * Countdown on-air beeps (3, 2, 1, Go LIVE).
     */
    fun playCountdownBeep(isFinal: Boolean) {
        scope.launch {
            try {
                val sampleRate = 44100
                val durationMs = if (isFinal) 400 else 150
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)
                val freq = if (isFinal) 1400.0 else 900.0

                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    val envelope = (1.0 - (time / (durationMs / 1000.0))).coerceIn(0.0, 1.0)
                    val sampleVal = sin(2.0 * Math.PI * freq * time) * envelope * Short.MAX_VALUE * 0.8
                    samples[i] = sampleVal.toInt().toShort()
                }

                playPcmBuffer(samples, sampleRate)
            } catch (e: Exception) {
                Log.e("NewsAudioEffects", "Error playing countdown beep", e)
            }
        }
    }

    fun resolveDefaultPitch(effectId: String): Float {
        return when (effectId) {
            "CHATGPT_COVE" -> 0.90f
            "CHATGPT_BREEZE" -> 1.08f
            "CHATGPT_EMBER" -> 0.98f
            "CHATGPT_JUNIPER" -> 1.18f
            "CHATGPT_SKY" -> 1.14f
            "CHATGPT_SOL" -> 0.95f
            "DEEP_ANCHOR" -> 0.76f
            "FEMALE_ANCHOR" -> 1.28f
            "RADIO_MIC" -> 1.04f
            "ECHO_STUDIO" -> 0.98f
            "CHIPMUNK" -> 1.55f
            "ROBOT" -> 0.86f
            "MEGAPHONE" -> 1.10f
            "SLOW_CINEMA" -> 0.68f
            else -> 1.0f
        }
    }

    /**
     * Synthesizes a short audible demonstration of the selected Voice Effect & pitch,
     * including ChatGPT AI voice mode warm conversational signatures.
     */
    fun playVoiceEffectPreview(effectId: String, pitch: Float = 1.0f) {
        scope.launch {
            try {
                val sampleRate = 44100
                val isChatGpt = effectId.startsWith("CHATGPT_")
                val durationMs = if (isChatGpt) 540 else 420
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)
                val baseFreq = when (effectId) {
                    "CHATGPT_COVE" -> 185.0
                    "CHATGPT_BREEZE" -> 265.0
                    "CHATGPT_EMBER" -> 215.0
                    "CHATGPT_JUNIPER" -> 305.0
                    "CHATGPT_SKY" -> 288.0
                    "CHATGPT_SOL" -> 200.0
                    "DEEP_ANCHOR" -> 150.0
                    "FEMALE_ANCHOR" -> 310.0
                    "CHIPMUNK" -> 520.0
                    "ROBOT" -> 220.0
                    "RADIO_MIC" -> 270.0
                    "MEGAPHONE" -> 340.0
                    "SLOW_CINEMA" -> 130.0
                    "ECHO_STUDIO" -> 260.0
                    else -> 240.0
                } * pitch.coerceIn(0.5f, 1.8f)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = if (t < 0.04) t / 0.04 else (1.0 - (t - 0.04) / (durationMs / 1000.0 - 0.04)).coerceIn(0.0, 1.0)
                    var raw = if (isChatGpt) {
                        // Two-note warm AI conversational inflection + soft third harmonic
                        val noteFreq = if (t < 0.24) baseFreq else baseFreq * 1.25
                        0.68 * sin(2.0 * Math.PI * noteFreq * t) +
                            0.24 * sin(2.0 * Math.PI * noteFreq * 2.0 * t) +
                            0.12 * sin(2.0 * Math.PI * noteFreq * 1.5 * t)
                    } else {
                        sin(2.0 * Math.PI * baseFreq * t) + 0.4 * sin(2.0 * Math.PI * baseFreq * 1.5 * t)
                    }
                    if (effectId == "ROBOT") {
                        raw *= sin(2.0 * Math.PI * 38.0 * t)
                    } else if (effectId == "RADIO_MIC" || effectId == "MEGAPHONE") {
                        raw = raw.coerceIn(-0.45, 0.45) * 1.8
                    } else if ((effectId == "ECHO_STUDIO" || effectId == "CHATGPT_SKY") && i > 4410) {
                        val wet = if (effectId == "CHATGPT_SKY") 0.26 else 0.45
                        raw = raw * 0.78 + (samples[i - 4410] / (Short.MAX_VALUE * 1.0)) * wet
                    }
                    samples[i] = (raw.coerceIn(-1.0, 1.0) * env * Short.MAX_VALUE * 0.65).toInt().toShort()
                }
                playPcmBuffer(samples, sampleRate)
            } catch (e: Exception) {
                Log.e("NewsAudioEffects", "Error playing voice preview", e)
            }
        }
    }

    @Volatile
    private var ttsEngine: android.speech.tts.TextToSpeech? = null

    /**
     * Speaks the Hindi news headline / script aloud using the selected ChatGPT / Voice FX pitch and cadence.
     */
    fun speakTextWithVoiceEffect(
        context: android.content.Context,
        text: String,
        effectId: String,
        pitch: Float
    ) {
        val cleanText = text.trim().ifEmpty { "नमस्कार, आप देख रहे हैं एच के न्यूज़ बुलेटिन।" }
        try {
            val effPitch = pitch.coerceIn(0.55f, 1.8f)
            val rate = when (effectId) {
                "CHATGPT_BREEZE", "CHATGPT_SKY" -> 1.04f
                "CHATGPT_COVE", "CHATGPT_SOL", "SLOW_CINEMA" -> 0.92f
                "CHATGPT_EMBER" -> 1.10f
                "CHATGPT_JUNIPER" -> 1.06f
                "CHIPMUNK" -> 1.25f
                else -> 1.0f
            }
            val existing = ttsEngine
            if (existing != null) {
                existing.language = java.util.Locale("hi", "IN")
                existing.setPitch(effPitch)
                existing.setSpeechRate(rate)
                existing.speak(cleanText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "hk_voice_fx")
                return
            }
            val appCtx = context.applicationContext
            var created: android.speech.tts.TextToSpeech? = null
            created = android.speech.tts.TextToSpeech(appCtx) { status ->
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    try {
                        created?.language = java.util.Locale("hi", "IN")
                        created?.setPitch(effPitch)
                        created?.setSpeechRate(rate)
                        created?.speak(cleanText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "hk_voice_fx")
                    } catch (_: Exception) {}
                }
            }
            ttsEngine = created
        } catch (e: Exception) {
            Log.w("NewsAudioEffects", "TTS unavailable, falling back to audio preview", e)
            playVoiceEffectPreview(effectId, pitch)
        }
    }

    private fun playPcmBuffer(samples: ShortArray, sampleRate: Int) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(samples, 0, samples.size)
        audioTrack.play()
        audioTrack.setNotificationMarkerPosition(samples.size)
        audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onPeriodicNotification(track: AudioTrack?) {}
            override fun onMarkerReached(track: AudioTrack?) {
                track?.release()
            }
        })
    }

    private fun fallbackTone(toneType: Int, durationMs: Int) {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
            toneGen.startTone(toneType, durationMs)
        } catch (_: Exception) {}
    }
}
