package com.skyfatelabs.soulpaperconnections.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.skyfatelabs.soulpaperconnections.model.Ticket

@Database(
    entities = [
        MessageEntity::class,
        AppointmentEntity::class,
        Ticket::class
    ],
    version = 3, // bumped for the new tickets table (dev-only; safe with fallbackToDestructiveMigration)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun ticketDao(): TicketDao
}
