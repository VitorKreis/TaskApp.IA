package com.example.myapplication.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.data.local.preferences.RoutinePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = RoutinePreferences(context)
                val (hour, minute) = prefs.planningTime.first()
                AlarmScheduler.schedule(context, hour, minute)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
