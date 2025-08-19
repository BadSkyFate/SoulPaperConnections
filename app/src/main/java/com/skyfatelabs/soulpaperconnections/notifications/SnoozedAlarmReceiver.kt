package com.skyfatelabs.soulpaperconnections.notifications


import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission

class SnoozedAlarmReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        Notifications.ensureChannel(context)
        val id = intent.getStringExtra("appointment_id") ?: return
        val title = intent.getStringExtra("appointment_title") ?: "Appointment"
        val notifId = (id + "-snooze").hashCode()
        Notifications.showAppointment(context, notifId, title, System.currentTimeMillis(), id)
    }
}
