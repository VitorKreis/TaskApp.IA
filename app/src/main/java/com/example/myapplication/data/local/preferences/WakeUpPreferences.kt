package com.example.myapplication.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class WakeUpPreferences @Inject constructor(
    private val context: Context
) {

    companion object {
        private val WAKE_UP_HOUR = intPreferencesKey("wake_up_hour")
        private val WAKE_UP_MINUTE = intPreferencesKey("wake_up_minute")

        const val DEFAULT_HOUR = 7
        const val DEFAULT_MINUTE = 0
    }

    /** Emits Pair(hour, minute) — defaults to 07:00. */
    val wakeUpTime: Flow<Pair<Int, Int>> = context.dataStore.data.map { prefs ->
        val hour = prefs[WAKE_UP_HOUR] ?: DEFAULT_HOUR
        val minute = prefs[WAKE_UP_MINUTE] ?: DEFAULT_MINUTE
        hour to minute
    }

    suspend fun setWakeUpTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[WAKE_UP_HOUR] = hour
            prefs[WAKE_UP_MINUTE] = minute
        }
    }
}
