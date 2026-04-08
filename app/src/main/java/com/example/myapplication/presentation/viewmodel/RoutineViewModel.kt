package com.example.myapplication.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.preferences.PeakFocus
import com.example.myapplication.data.local.preferences.QuietHours
import com.example.myapplication.data.repository.RoutineRepository
import com.example.myapplication.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    application: Application,
    private val repository: RoutineRepository
) : AndroidViewModel(application) {

    val planningTime: StateFlow<Pair<Int, Int>> = repository.planningTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7 to 0)

    val quietHours: StateFlow<QuietHours> = repository.quietHours
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuietHours())

    val peakFocus: StateFlow<PeakFocus> = repository.peakFocus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PeakFocus.MORNING)

    val peakFocusCustomTime: StateFlow<Pair<Int, Int>> = repository.peakFocusCustomTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 9 to 0)

    fun setPlanningTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.setPlanningTime(hour, minute)
            AlarmScheduler.schedule(getApplication(), hour, minute)
        }
    }

    fun setQuietHours(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        viewModelScope.launch { repository.setQuietHours(startHour, startMinute, endHour, endMinute) }
    }

    fun setPeakFocus(focus: PeakFocus) {
        viewModelScope.launch { repository.setPeakFocus(focus) }
    }

    fun setPeakFocusCustomTime(hour: Int, minute: Int) {
        viewModelScope.launch { repository.setPeakFocusCustomTime(hour, minute) }
    }
}
