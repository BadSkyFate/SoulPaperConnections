package com.skyfatelabs.soulpaperconnections.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.skyfatelabs.soulpaperconnections.MainActivity
import com.skyfatelabs.soulpaperconnections.R

object Notifications {
    const val CHANNEL_ID = "appointments_channel"
    const val CHANNEL_NAME = "Appointments"
    const val CHANNEL_DESC = "Reminders for upcoming appointments"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = CHANNEL_DESC }
            mgr.createNotificationChannel(channel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showAppointment(context: Context, notifId: Int, title: String, whenMillis: Long, apptId: String) {
        val openAppIntent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // NEW: Snooze action → broadcast to SnoozeReceiver
        val snoozeIntent = Intent(context, SnoozeReceiver::class.java).apply {
            putExtra("appointment_id", apptId)
            putExtra("appointment_title", title)
        }
        val snoozePi = PendingIntent.getBroadcast(
            context, ("snooze-" + apptId).hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title.ifBlank { "Appointment" })
            .setContentText("Starts soon")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setWhen(whenMillis)
            .addAction(0, "Snooze 5 min", snoozePi) // NEW
            .build()

        NotificationManagerCompat.from(context).notify(notifId, notif)
    }

}
