package com.skyfatelabs.soulpaperconnections.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.skyfatelabs.soulpaperconnections.Graph
import com.skyfatelabs.soulpaperconnections.model.Recurrence
import java.time.ZoneId

class BootCompletedReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) return

        // Keep receiver alive until work completes
        val pending = goAsync()
        val appCtx = context.applicationContext

        CoroutineScope(Dispatchers.Default).launch {
            try {
                // Recreate channel & alarm helpers
                Notifications.ensureChannel(appCtx)
                AlarmCenter.init(appCtx)

                val zone = ZoneId.systemDefault()
                val nowMs = System.currentTimeMillis()

                // Take a snapshot of current appointments (no DAO change needed)
                val appts = Graph.apptRepo.appointments.first()

                appts.forEach { appt ->
                    val startMs = appt.start.atZone(zone).toInstant().toEpochMilli()
                    val leadMs = appt.reminderMinutes.coerceAtLeast(0) * 60_000L
                    val triggerMs = startMs - leadMs

                    val toSchedule = if (triggerMs > nowMs) {
                        // Future reminder—schedule as-is
                        appt
                    } else if (appt.recurrence != Recurrence.NONE) {
                        // Reminder time passed, but recurring—advance then schedule
                        Graph.apptRepo.advanceToNextOccurrence(appt)
                    } else {
                        // Past & non-recurring—skip
                        null
                    }

                    toSchedule?.let { next ->
                        val nextStartMs = next.start.atZone(zone).toInstant().toEpochMilli()
                        AlarmCenter.scheduleAppointment(
                            next.id,
                            next.title,
                            nextStartMs,
                            next.reminderMinutes
                        )
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }
}
