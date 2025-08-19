package com.skyfatelabs.soulpaperconnections.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val text: String,
    val timestampEpochMillis: Long,
    val fromMe: Boolean,
    val read: Boolean
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val startEpochMillis: Long,
    val reminderMinutes: Int = 10 // NEW: default 10 minutes
)

