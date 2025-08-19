package com.skyfatelabs.soulpaperconnections.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.Instant

object AlarmCenter {
    var leadTimeMillis: Long = 10 * 60_000L

    private lateinit var appContext: Context
    fun init(context: Context) { appContext = context.applicationContext }

    fun scheduleAppointment(id: String, title: String, startEpochMillis: Long, reminderMinutes: Int) {
        val lead = reminderMinutes.coerceAtLeast(0) * 60_000L
        val triggerAt = (startEpochMillis - lead).coerceAtLeast(Instant.now().toEpochMilli())
        val am = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntentFor(id, title)
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }

    fun scheduleAppointment(id: String, title: String, startEpochMillis: Long) =
        scheduleAppointment(id, title, startEpochMillis, (leadTimeMillis / 60_000L).toInt())

    fun cancelAppointment(id: String) {
        val am = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntentFor(id, ""))
    }

    fun cancelSnooze(id: String) { // NEW
        val am = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            appContext,
            (id + "-snooze").hashCode(),
            Intent(appContext, SnoozedAlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) am.cancel(pi)
    }

    private fun pendingIntentFor(id: String, title: String): PendingIntent {
        val intent = Intent(appContext, AppointmentAlarmReceiver::class.java).apply {
            putExtra("appointment_id", id)
            putExtra("appointment_title", title)
        }
        val requestCode = id.hashCode()
        return PendingIntent.getBroadcast(
            appContext, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
