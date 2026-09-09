package ir.atiran.hamrah.viewer.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator

/**
 * رسیور آلارم یادآور — زمانی که یادآور کاربر به موعد می‌رسد:
 * صدای آلارم (بر اساس نوع انتخابی)، ویبره و اعلان اندروید با متن یادآور.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val text = intent.getStringExtra("text") ?: "یادآور M•REPORT"
        val cat = intent.getStringExtra("cat") ?: "سایر"
        val sound = intent.getStringExtra("sound") ?: "معمولی"
        val rid = intent.getIntExtra("rid", 1000)

        // ---------- صدای آلارم — بر اساس نوع انتخابی کاربر
        Thread {
            try {
                val tg = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                when (sound) {
                    "ملایم" -> {
                        tg.startTone(ToneGenerator.TONE_PROP_BEEP, 500)
                        Thread.sleep(700)
                    }
                    "فوری" -> {
                        repeat(5) {
                            tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 420)
                            Thread.sleep(520)
                        }
                    }
                    else -> {
                        repeat(3) {
                            tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 380)
                            Thread.sleep(560)
                        }
                    }
                }
                tg.release()
            } catch (_: Throwable) {
            }
        }.start()

        // ---------- ویبره
        try {
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            if (v != null) {
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    v.vibrate(
                        android.os.VibrationEffect.createWaveform(longArrayOf(0, 400, 250, 400, 250, 600), -1)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(longArrayOf(0, 400, 250, 400, 250, 600), -1)
                }
            }
        } catch (_: Throwable) {
        }

        // ---------- اعلان اندروید
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "mreport_reminders",
                "یادآورهای M•REPORT",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "آلارم یادآورهای شخصی"
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
            val openApp = PendingIntent.getActivity(
                context, 0,
                Intent(context, ir.atiran.hamrah.viewer.MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notif = Notification.Builder(context, "mreport_reminders")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("⏰ یادآور — $cat")
                .setContentText(text)
                .setStyle(Notification.BigTextStyle().bigText(text))
                .setAutoCancel(true)
                .setContentIntent(openApp)
                .build()
            nm.notify(rid, notif)
        } catch (_: Throwable) {
        }
    }
}
