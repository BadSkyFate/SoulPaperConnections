@file:Suppress("NewApi") // java.time on minSdk < 26 requires coreLibraryDesugaring

package com.skyfatelabs.soulpaperconnections.data

import com.skyfatelabs.soulpaperconnections.model.Appointment
import com.skyfatelabs.soulpaperconnections.model.Message
import com.skyfatelabs.soulpaperconnections.model.Recurrence
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// Cache the system default zone
private val zone: ZoneId by lazy { ZoneId.systemDefault() }

/** -------------------- Message mappers -------------------- */

fun MessageEntity.toModel() = Message(
    id = id,
    text = text,
    timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampEpochMillis), zone),
    fromMe = fromMe,
    read = read
)

fun Message.toEntity() = MessageEntity(
    id = id,
    text = text,
    timestampEpochMillis = timestamp.atZone(zone).toInstant().toEpochMilli(),
    fromMe = fromMe,
    read = read
)

/** -------------------- Appointment mappers --------------------
 * This version is compatible with a DB schema that does NOT yet
 * have a `recurrence` column on AppointmentEntity. We default to
 * Recurrence.NONE on read, and ignore recurrence on write.
 * When you add the column, update these to read/write it.
 * ------------------------------------------------------------- */

fun AppointmentEntity.toModel(): Appointment = Appointment(
    id = id,
    title = title,
    start = LocalDateTime.ofInstant(Instant.ofEpochMilli(startEpochMillis), zone),
    reminderMinutes = reminderMinutes,
    recurrence = Recurrence.NONE // DB doesn't store it yet; default safely
)

fun Appointment.toEntity(): AppointmentEntity = AppointmentEntity(
    id = id,
    title = title,
    startEpochMillis = start.atZone(zone).toInstant().toEpochMilli(),
    reminderMinutes = reminderMinutes
    // No recurrence field in the entity yet
)
