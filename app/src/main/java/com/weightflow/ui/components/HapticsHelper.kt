package com.weightflow.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun rememberWFHaptics(): WFHaptics {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    return remember(haptic, context) { WFHaptics(haptic, context) }
}

class WFHaptics(
    private val haptic: HapticFeedback,
    private val context: Context,
) {
    /** Light tick — used on each drum notch */
    fun tick() = haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

    /** Medium confirm — used on save */
    fun confirm() = haptic.performHapticFeedback(HapticFeedbackType.LongPress)

    /** Heavy celebrate — used on new personal low */
    fun celebrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(VibratorManager::class.java)
                ?.defaultVibrator ?: return
            vm.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 60, 80, 120),
                    intArrayOf(0, 200, 0, 255),
                    -1,
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            val vm = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            vm.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 60, 80, 120),
                    intArrayOf(0, 200, 0, 255),
                    -1,
                ),
            )
        }
    }
}
