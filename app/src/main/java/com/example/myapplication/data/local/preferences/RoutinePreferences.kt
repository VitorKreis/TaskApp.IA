package com.example.myapplication.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.routineDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "routine_settings"
)

/**
 * Horário de pico de foco do usuário.
 */
enum class PeakFocus(val label: String) {
    MORNING("Manhã"),
    AFTERNOON("Tarde"),
    EVENING("Noite"),
    CUSTOM("Personalizado");

    companion object {
        fun fromString(value: String): PeakFocus =
            entries.firstOrNull { it.name == value } ?: MORNING
    }
}

/**
 * Representação da janela de silêncio.
 */
data class QuietHours(
    val startHour: Int = DEFAULT_QUIET_START_HOUR,
    val startMinute: Int = DEFAULT_QUIET_START_MINUTE,
    val endHour: Int = DEFAULT_QUIET_END_HOUR,
    val endMinute: Int = DEFAULT_QUIET_END_MINUTE
) {
    companion object {
        const val DEFAULT_QUIET_START_HOUR = 22
        const val DEFAULT_QUIET_START_MINUTE = 0
        const val DEFAULT_QUIET_END_HOUR = 7
        const val DEFAULT_QUIET_END_MINUTE = 0
    }

    /**
     * Verifica se um horário (hour:minute) está dentro da janela de silêncio.
     * Funciona corretamente mesmo quando a janela cruza a meia-noite (ex: 22:00–07:00).
     */
    fun isInQuietWindow(hour: Int, minute: Int): Boolean {
        val now = hour * 60 + minute
        val start = startHour * 60 + startMinute
        val end = endHour * 60 + endMinute

        return if (start <= end) {
            // Janela no mesmo dia (ex: 12:00–14:00)
            now in start until end
        } else {
            // Janela cruzando meia-noite (ex: 22:00–07:00)
            now >= start || now < end
        }
    }
}

@Singleton
class RoutinePreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        // ── Planning Time (Horário de Planejamento / Morning Briefing) ──
        private val PLANNING_HOUR = intPreferencesKey("planning_hour")
        private val PLANNING_MINUTE = intPreferencesKey("planning_minute")
        const val DEFAULT_PLANNING_HOUR = 7
        const val DEFAULT_PLANNING_MINUTE = 0

        // ── Quiet Hours (Janela de Silêncio) ────────────────────────────
        private val QUIET_START_HOUR = intPreferencesKey("quiet_start_hour")
        private val QUIET_START_MINUTE = intPreferencesKey("quiet_start_minute")
        private val QUIET_END_HOUR = intPreferencesKey("quiet_end_hour")
        private val QUIET_END_MINUTE = intPreferencesKey("quiet_end_minute")

        // ── Peak Focus (Pico de Foco) ───────────────────────────────────
        private val PEAK_FOCUS = stringPreferencesKey("peak_focus")
        private val PEAK_FOCUS_CUSTOM_HOUR = intPreferencesKey("peak_focus_custom_hour")
        private val PEAK_FOCUS_CUSTOM_MINUTE = intPreferencesKey("peak_focus_custom_minute")
        const val DEFAULT_PEAK_FOCUS_CUSTOM_HOUR = 9
        const val DEFAULT_PEAK_FOCUS_CUSTOM_MINUTE = 0
    }

    // ── Flows ───────────────────────────────────────────────────────────────

    /** Emite Pair(hour, minute) do horário de planejamento matinal. */
    val planningTime: Flow<Pair<Int, Int>> = context.routineDataStore.data.map { prefs ->
        val hour = prefs[PLANNING_HOUR] ?: DEFAULT_PLANNING_HOUR
        val minute = prefs[PLANNING_MINUTE] ?: DEFAULT_PLANNING_MINUTE
        hour to minute
    }

    /** Emite a configuração completa da janela de silêncio. */
    val quietHours: Flow<QuietHours> = context.routineDataStore.data.map { prefs ->
        QuietHours(
            startHour = prefs[QUIET_START_HOUR] ?: QuietHours.DEFAULT_QUIET_START_HOUR,
            startMinute = prefs[QUIET_START_MINUTE] ?: QuietHours.DEFAULT_QUIET_START_MINUTE,
            endHour = prefs[QUIET_END_HOUR] ?: QuietHours.DEFAULT_QUIET_END_HOUR,
            endMinute = prefs[QUIET_END_MINUTE] ?: QuietHours.DEFAULT_QUIET_END_MINUTE
        )
    }

    /** Emite o tipo de pico de foco selecionado. */
    val peakFocus: Flow<PeakFocus> = context.routineDataStore.data.map { prefs ->
        PeakFocus.fromString(prefs[PEAK_FOCUS] ?: PeakFocus.MORNING.name)
    }

    /** Emite o horário personalizado de pico de foco (usado quando peakFocus == CUSTOM). */
    val peakFocusCustomTime: Flow<Pair<Int, Int>> = context.routineDataStore.data.map { prefs ->
        val hour = prefs[PEAK_FOCUS_CUSTOM_HOUR] ?: DEFAULT_PEAK_FOCUS_CUSTOM_HOUR
        val minute = prefs[PEAK_FOCUS_CUSTOM_MINUTE] ?: DEFAULT_PEAK_FOCUS_CUSTOM_MINUTE
        hour to minute
    }

    // ── Setters ─────────────────────────────────────────────────────────────

    suspend fun setPlanningTime(hour: Int, minute: Int) {
        context.routineDataStore.edit { prefs ->
            prefs[PLANNING_HOUR] = hour
            prefs[PLANNING_MINUTE] = minute
        }
    }

    suspend fun setQuietHours(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        context.routineDataStore.edit { prefs ->
            prefs[QUIET_START_HOUR] = startHour
            prefs[QUIET_START_MINUTE] = startMinute
            prefs[QUIET_END_HOUR] = endHour
            prefs[QUIET_END_MINUTE] = endMinute
        }
    }

    suspend fun setPeakFocus(peakFocus: PeakFocus) {
        context.routineDataStore.edit { prefs ->
            prefs[PEAK_FOCUS] = peakFocus.name
        }
    }

    suspend fun setPeakFocusCustomTime(hour: Int, minute: Int) {
        context.routineDataStore.edit { prefs ->
            prefs[PEAK_FOCUS] = PeakFocus.CUSTOM.name
            prefs[PEAK_FOCUS_CUSTOM_HOUR] = hour
            prefs[PEAK_FOCUS_CUSTOM_MINUTE] = minute
        }
    }
}
