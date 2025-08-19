@file:Suppress("NewApi") // Using java.time with coreLibraryDesugaring (minSdk 24)

package com.skyfatelabs.soulpaperconnections.model

import java.time.LocalDateTime
import java.util.UUID

enum class Recurrence { NONE, DAILY, WEEKLY, MONTHLY }

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    // If you see an API 26 warning here, ensure Java 8 desugaring is enabled in Gradle.
    // (coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.0.x')
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val fromMe: Boolean,
    val read: Boolean = false
)

data class Appointment(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val start: LocalDateTime,
    val reminderMinutes: Int = 10,
    val recurrence: Recurrence = Recurrence.NONE
)
