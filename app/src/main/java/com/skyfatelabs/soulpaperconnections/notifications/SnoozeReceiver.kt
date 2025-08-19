package com.skyfatelabs.soulpaperconnections.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat

class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Ensure notification channel / alarm helper are ready
        Notifications.ensureChannel(context)
        AlarmCenter.init(context)

        val id = intent.getStringExtra("appointment_id") ?: return
        val title = intent.getStringExtra("appointment_title") ?: "Appointment"
        // Dynamic snooze duration from the action; default to 10 minutes if missing
        val minutes = intent.getIntExtra("snooze_minutes", 10).coerceAtLeast(1)

        // Dismiss the current notification (we use id.hashCode() as notifId elsewhere)
        NotificationManagerCompat.from(context).cancel(id.hashCode())

        // Cancel any existing snooze for this appointment to avoid duplicates
        AlarmCenter.cancelSnooze(id)

        // Schedule a new "snoozed" alarm that will re-show the reminder
        val triggerAtMillis = System.currentTimeMillis() + minutes * 60_000L
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val snoozedIntent = Intent(context, SnoozedAlarmReceiver::class.java).apply {
            putExtra("appointment_id", id)
            putExtra("appointment_title", title)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            (id + "-snooze").hashCode(),
            snoozedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Android 12+ requires the SCHEDULE_EXACT_ALARM capability for exact alarms.
        // Be robust: if we can't schedule exact, fall back to inexact setAndAllowWhileIdle.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val canExact = try {
                alarmManager.canScheduleExactAlarms()
            } catch (_: Throwable) {
                false
            }

            if (canExact) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
                } catch (_: SecurityException) {
                    // App isn't allowed to schedule exact alarms — fall back gracefully.
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
                }
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
            }
        } else {
            // Pre-Android 12: exact while idle is permitted.
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        }
    }
}
