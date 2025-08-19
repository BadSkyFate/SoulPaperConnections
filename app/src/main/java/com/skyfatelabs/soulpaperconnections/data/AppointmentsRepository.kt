package com.skyfatelabs.soulpaperconnections.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.skyfatelabs.soulpaperconnections.model.Appointment
import com.skyfatelabs.soulpaperconnections.model.Recurrence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class AppointmentsRepository(private val dao: AppointmentDao) {

    val appointments: Flow<List<Appointment>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun upsert(appt: Appointment) {
        dao.upsert(appt.toEntity())
    }

    suspend fun deleteById(id: String) = dao.deleteById(id)

    /**
     * Advance a recurring appointment to the next future occurrence, persist it,
     * and return the advanced copy. Non-recurring appointments are returned unchanged.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun advanceToNextOccurrence(appt: Appointment): Appointment {
        if (appt.recurrence == Recurrence.NONE) return appt

        val now = LocalDateTime.now()
        val nextStart = nextAfter(appt.start, appt.recurrence, now)
        val advanced = appt.copy(start = nextStart)

        // Persist the advanced occurrence so the rest of the app sees the new time
        dao.upsert(advanced.toEntity())
        return advanced
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun nextAfter(
        start: LocalDateTime,
        recurrence: Recurrence,
        now: LocalDateTime
    ): LocalDateTime {
        var t = start
        var guard = 0 // safety to avoid infinite loops

        when (recurrence) {
            Recurrence.DAILY -> {
                while (!t.isAfter(now) && guard++ < 2000) t = t.plusDays(1)
            }
            Recurrence.WEEKLY -> {
                while (!t.isAfter(now) && guard++ < 500) t = t.plusWeeks(1)
            }
            Recurrence.MONTHLY -> {
                // LocalDateTime.plusMonths() clamps end-of-month dates automatically
                while (!t.isAfter(now) && guard++ < 240) t = t.plusMonths(1)
            }
            Recurrence.NONE -> { /* handled above */ }
        }
        return t
    }
}
