package com.skyfatelabs.soulpaperconnections.viewmodel


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.skyfatelabs.soulpaperconnections.Graph
import com.skyfatelabs.soulpaperconnections.data.AppointmentsRepository
import com.skyfatelabs.soulpaperconnections.model.Appointment
import com.skyfatelabs.soulpaperconnections.model.Recurrence
import com.skyfatelabs.soulpaperconnections.notifications.AlarmCenter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

class AppointmentsViewModel(private val repo: AppointmentsRepository) : ViewModel() {

    val appointments: StateFlow<List<Appointment>> =
        repo.appointments.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @RequiresApi(Build.VERSION_CODES.O)
    fun addAppointment(
        title: String,
        date: LocalDate,
        time: LocalTime,
        reminderMinutes: Int,
        recurrence: Recurrence
    ) {
        viewModelScope.launch {
            val start = LocalDateTime.of(date, time)
            val appt = Appointment(
                id = UUID.randomUUID().toString(),
                title = title.ifBlank { "Untitled" },
                start = start,
                reminderMinutes = reminderMinutes,
                recurrence = recurrence
            )
            repo.upsert(appt)
            scheduleIfApplicable(appt)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateAppointment(
        id: String,
        title: String,
        date: LocalDate,
        time: LocalTime,
        reminderMinutes: Int,
        recurrence: Recurrence
    ) {
        viewModelScope.launch {
            AlarmCenter.cancelAppointment(id)
            AlarmCenter.cancelSnooze(id)

            val updated = Appointment(
                id = id,
                title = title.ifBlank { "Untitled" },
                start = LocalDateTime.of(date, time),
                reminderMinutes = reminderMinutes,
                recurrence = recurrence
            )
            repo.upsert(updated)
            scheduleIfApplicable(updated)
        }
    }

    fun deleteAppointment(id: String) {
        viewModelScope.launch {
            AlarmCenter.cancelAppointment(id)
            AlarmCenter.cancelSnooze(id)
            repo.deleteById(id)
        }
    }

    /** Re-insert a previously deleted appointment and re-schedule its alarm. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun restoreAppointment(appt: Appointment) {
        viewModelScope.launch {
            repo.upsert(appt)
            scheduleIfApplicable(appt)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun seedDemo() {
        viewModelScope.launch {
            if (appointments.value.isEmpty()) {
                val now = LocalDateTime.now()
                val a = Appointment(
                    title = "Coffee chat",
                    start = now.plusHours(2),
                    reminderMinutes = 10,
                    recurrence = Recurrence.NONE
                )
                val b = Appointment(
                    title = "Dentist",
                    start = now.plusDays(1).withHour(9).withMinute(0),
                    reminderMinutes = 30,
                    recurrence = Recurrence.WEEKLY
                )
                repo.upsert(a); repo.upsert(b)
                scheduleIfApplicable(a)
                scheduleIfApplicable(b)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun scheduleIfApplicable(appt: Appointment) {
        val zone = ZoneId.systemDefault()
        val nowMs = Instant.now().toEpochMilli()
        val startMs = appt.start.atZone(zone).toInstant().toEpochMilli()
        val leadMs = appt.reminderMinutes.coerceAtLeast(0) * 60_000L

        if ((startMs - leadMs) > nowMs) {
            AlarmCenter.scheduleAppointment(appt.id, appt.title, startMs, appt.reminderMinutes)
            return
        }
        if (appt.recurrence != Recurrence.NONE) {
            val advanced = repo.advanceToNextOccurrence(appt)
            val advStartMs = advanced.start.atZone(zone).toInstant().toEpochMilli()
            AlarmCenter.scheduleAppointment(advanced.id, advanced.title, advStartMs, advanced.reminderMinutes)
        }
    }

    companion object {
        fun factory() = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppointmentsViewModel(Graph.apptRepo) as T
            }
        }
    }

}
