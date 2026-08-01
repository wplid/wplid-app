package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.R

object FeedbackHelper {

    private const val CHANNEL_ID = "wplid_completion_channel"
    private const val CHANNEL_NAME = "wplid Notifications"

    fun playCompletionFeedback(context: Context, isTimer: Boolean = false) {
        // 1. Play Sound
        try {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, soundUri)
            if (ringtone != null) {
                ringtone.play()
            } else {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                if (isTimer) {
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 600)
                } else {
                    toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 300)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Vibrate
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = if (isTimer) {
                        VibrationEffect.createWaveform(longArrayOf(0, 250, 150, 300), -1)
                    } else {
                        VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(if (isTimer) 500 else 200)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Post System Notification
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (notificationManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Notifications when focus sessions or tasks are completed"
                        enableVibration(true)
                        enableLights(true)
                    }
                    notificationManager.createNotificationChannel(channel)
                }

                val title = if (isTimer) "Focus Timer Finished! 🎯" else "Task Completed! Check! ✅"
                val body = if (isTimer) "Great job staying focused! Time for a break." else "You completed a task in wplid."

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.app_icon_wplid_glass_1785596074057)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(if (isTimer) 2001 else 2002, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
