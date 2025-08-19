package com.skyfatelabs.soulpaperconnections.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.skyfatelabs.soulpaperconnections.Graph
import com.skyfatelabs.soulpaperconnections.model.Recurrence
import com.skyfatelabs.soulpaperconnections.notifications.AlarmCenter
import com.skyfatelabs.soulpaperconnections.settings.Settings
import com.skyfatelabs.soulpaperconnections.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    val settings: StateFlow<Settings> =
        repo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    /**
     * OFF: cancel all scheduled alarms and snoozes.
     * ON:  schedule future ones; if past but recurring, advance first.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repo.setNotificationsEnabled(enabled)

            val apptRepo = Graph.apptRepo
            val current = apptRepo.appointments.first() // snapshot of current list

            if (!enabled) {
                // Cancel everything
                current.forEach { appt ->
                    AlarmCenter.cancelAppointment(appt.id)
                    AlarmCenter.cancelSnooze(appt.id)
                }
            } else {
                val zone = ZoneId.systemDefault()
                val nowMs = Instant.now().toEpochMilli()

                current.forEach { appt ->
                    val startMs = appt.start.atZone(zone).toInstant().toEpochMilli()
                    val leadMs = appt.reminderMinutes.coerceAtLeast(0) * 60_000L
                    val trigger = startMs - leadMs

                    val toSchedule = if (trigger > nowMs) {
                        appt
                    } else if (appt.recurrence != Recurrence.NONE) {
                        apptRepo.advanceToNextOccurrence(appt)
                    } else null

                    toSchedule?.let { next ->
                        val startNextMs = next.start.atZone(zone).toInstant().toEpochMilli()
                        AlarmCenter.scheduleAppointment(
                            next.id,
                            next.title,
                            startNextMs,
                            next.reminderMinutes
                        )
                    }
                }
            }
        }
    }

    fun setDefaultReminder(min: Int) = viewModelScope.launch { repo.setDefaultReminderMinutes(min) }
    fun setSnoozeMinutes(list: List<Int>) = viewModelScope.launch { repo.setSnoozeMinutes(list) }
    fun setFirstDay(dow: DayOfWeek) = viewModelScope.launch { repo.setFirstDayOfWeek(dow) }

    companion object {
        fun factory() = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SettingsViewModel(Graph.settingsRepo) as T
        }
    }
}
