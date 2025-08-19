package com.skyfatelabs.soulpaperconnections.data

import androidx.room.*
import com.skyfatelabs.soulpaperconnections.model.Ticket
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets WHERE active = 1 ORDER BY name")
    fun activeTickets(): Flow<List<Ticket>>

    @Query("SELECT * FROM tickets ORDER BY name")
    fun allTickets(): Flow<List<Ticket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(ticket: Ticket)

    @Delete
    suspend fun delete(ticket: Ticket)
}
