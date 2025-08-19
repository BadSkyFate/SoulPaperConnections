package com.skyfatelabs.soulpaperconnections.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM messages ORDER BY timestampEpochMillis ASC")
    fun observeAll(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg msgs: MessageEntity)

    @Query("UPDATE messages SET read = 1 WHERE read = 0")
    suspend fun markAllRead()

    @Query("DELETE FROM messages")
    suspend fun clearAll()
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY startEpochMillis ASC")
    fun observeAll(): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg appts: AppointmentEntity)

    @Delete
    suspend fun delete(appt: AppointmentEntity)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM appointments")
    suspend fun getAllOnce(): List<AppointmentEntity>

}
