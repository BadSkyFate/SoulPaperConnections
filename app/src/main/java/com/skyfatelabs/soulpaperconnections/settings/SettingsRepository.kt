package com.skyfatelabs.soulpaperconnections.settings

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek

// Single-process Preferences DataStore
private val Context.dataStore by preferencesDataStore(name = "settings")

data class Settings @RequiresApi(Build.VERSION_CODES.O) constructor(
    val defaultReminderMinutes: Int = 10,
    val snoozeMinutes: List<Int> = listOf(10, 15, 30),
    val firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,
    val notificationsEnabled: Boolean = true
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val DEFAULT_REMINDER = intPreferencesKey("default_reminder_minutes")
        val SNOOZE_MINUTES_CSV = stringPreferencesKey("snooze_minutes_csv")
        val FIRST_DAY = stringPreferencesKey("first_day_of_week") // enum name
        val NOTIFS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        val defRem = prefs[Keys.DEFAULT_REMINDER] ?: 10
        val csv = prefs[Keys.SNOOZE_MINUTES_CSV] ?: "10,15,30"
        val snooze = csv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it > 0 }
            .ifEmpty { listOf(10, 15, 30) }

        val dow = prefs[Keys.FIRST_DAY]
            ?.let { runCatching { DayOfWeek.valueOf(it) }.getOrNull() }
            ?: DayOfWeek.SUNDAY

        val enabled = prefs[Keys.NOTIFS_ENABLED] ?: true

        Settings(defRem, snooze, dow, enabled)
    }

    suspend fun setDefaultReminderMinutes(min: Int) =
        context.dataStore.edit { it[Keys.DEFAULT_REMINDER] = min }

    suspend fun setSnoozeMinutes(minutes: List<Int>) =
        context.dataStore.edit { it[Keys.SNOOZE_MINUTES_CSV] = minutes.sorted().joinToString(",") }

    suspend fun setFirstDayOfWeek(dow: DayOfWeek) =
        context.dataStore.edit { it[Keys.FIRST_DAY] = dow.name }

    suspend fun setNotificationsEnabled(enabled: Boolean) =
        context.dataStore.edit { it[Keys.NOTIFS_ENABLED] = enabled }
}
