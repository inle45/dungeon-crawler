package com.dungeoncrawler.wearos.core.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class HapticPattern {
    BOSS_ALERT,
    CRITICAL_HIT,
    STANDARD_HIT,
    PARRY_SUCCESS,
    DAMAGE_TAKEN,
    SPELL_CAST,
    MICRO_EVENT_LOOT,
    MICRO_EVENT_TRAP,
}

/**
 * Wraps [Vibrator] with one distinct waveform per gameplay event so the player can read combat
 * outcomes without looking at the screen.
 */
@Singleton
class HapticFeedbackManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService<VibratorManager>()?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService<Vibrator>()
    } ?: throw IllegalStateException("No Vibrator available on this device")

    fun play(pattern: HapticPattern) {
        val effect = when (pattern) {
            HapticPattern.BOSS_ALERT -> waveform(
                timings = longArrayOf(0, 120, 80, 120, 80, 240),
                amplitudes = intArrayOf(0, 255, 0, 255, 0, 255),
            )
            HapticPattern.CRITICAL_HIT -> waveform(
                timings = longArrayOf(0, 40, 40, 120),
                amplitudes = intArrayOf(0, 255, 0, 255),
            )
            HapticPattern.STANDARD_HIT -> oneShot(durationMillis = 60, amplitude = 180)
            HapticPattern.PARRY_SUCCESS -> waveform(
                timings = longArrayOf(0, 30, 30, 30),
                amplitudes = intArrayOf(0, 200, 0, 200),
            )
            HapticPattern.DAMAGE_TAKEN -> oneShot(durationMillis = 180, amplitude = 255)
            HapticPattern.SPELL_CAST -> waveform(
                timings = longArrayOf(0, 20, 20, 20, 20, 20, 20, 80),
                amplitudes = intArrayOf(0, 120, 0, 160, 0, 200, 0, 255),
            )
            HapticPattern.MICRO_EVENT_LOOT -> oneShot(durationMillis = 40, amplitude = 120)
            HapticPattern.MICRO_EVENT_TRAP -> waveform(
                timings = longArrayOf(0, 100, 60, 100),
                amplitudes = intArrayOf(0, 200, 0, 200),
            )
        }
        vibrator.vibrate(effect)
    }

    private fun oneShot(durationMillis: Long, amplitude: Int): VibrationEffect =
        VibrationEffect.createOneShot(durationMillis, amplitude)

    private fun oneShot(durationMillis: Int, amplitude: Int): VibrationEffect =
        oneShot(durationMillis.toLong(), amplitude)

    private fun waveform(timings: LongArray, amplitudes: IntArray): VibrationEffect =
        VibrationEffect.createWaveform(timings, amplitudes, -1)
}
