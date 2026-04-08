package com.example.myapplication.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.preferences.WakeUpPreferences
import com.example.myapplication.notification.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    application: Application,
    private val wakeUpPreferences: WakeUpPreferences
) : AndroidViewModel(application) {

    val wakeUpTime: StateFlow<Pair<Int, Int>> = wakeUpPreferences.wakeUpTime
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            WakeUpPreferences.DEFAULT_HOUR to WakeUpPreferences.DEFAULT_MINUTE
        )

    fun setWakeUpTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            wakeUpPreferences.setWakeUpTime(hour, minute)
            AlarmScheduler.schedule(getApplication(), hour, minute)
        }
    }
}
