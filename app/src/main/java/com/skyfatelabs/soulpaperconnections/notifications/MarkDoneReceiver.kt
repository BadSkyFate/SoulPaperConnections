package com.skyfatelabs.soulpaperconnections.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class MarkDoneReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("appointment_id") ?: return
        // Dismiss any active notifs (main or snoozed) for this id
        NotificationManagerCompat.from(context).cancel(id.hashCode())
        NotificationManagerCompat.from(context).cancel((id + "-snooze").hashCode())
        // Cancel any pending snoozed alarm
        AlarmCenter.init(context)
        AlarmCenter.cancelSnooze(id)
        // No DB changes needed: the series was already advanced on the original fire
    }
}
