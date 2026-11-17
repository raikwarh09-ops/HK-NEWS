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
