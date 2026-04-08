package com.example.myapplication.data.repository

import com.example.myapplication.data.local.preferences.PeakFocus
import com.example.myapplication.data.local.preferences.QuietHours
import com.example.myapplication.data.local.preferences.RoutinePreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    private val prefs: RoutinePreferences
) {
    // ── Flows (leitura reativa) ─────────────────────────────────────────

    /** Horário de planejamento matinal — Pair(hour, minute). */
    val planningTime: Flow<Pair<Int, Int>> = prefs.planningTime

    /** Janela de silêncio com horário de início e fim. */
    val quietHours: Flow<QuietHours> = prefs.quietHours

    /** Tipo de pico de foco selecionado. */
    val peakFocus: Flow<PeakFocus> = prefs.peakFocus

    /** Horário personalizado de pico de foco (relevante quando peakFocus == CUSTOM). */
    val peakFocusCustomTime: Flow<Pair<Int, Int>> = prefs.peakFocusCustomTime

    // ── Escritas ────────────────────────────────────────────────────────

    suspend fun setPlanningTime(hour: Int, minute: Int) {
        prefs.setPlanningTime(hour, minute)
    }

    suspend fun setQuietHours(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        prefs.setQuietHours(startHour, startMinute, endHour, endMinute)
    }

    suspend fun setPeakFocus(peakFocus: PeakFocus) {
        prefs.setPeakFocus(peakFocus)
    }

    suspend fun setPeakFocusCustomTime(hour: Int, minute: Int) {
        prefs.setPeakFocusCustomTime(hour, minute)
    }
}
