package com.example.myapplication.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.data.local.database.AppDatabase
import com.example.myapplication.data.local.preferences.RoutinePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class MorningNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val dao = db.taskDao()

                // Calculate today's start/end
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfDay = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, 1)
                val endOfDay = cal.timeInMillis
                val now = System.currentTimeMillis()

                // Query tasks from Room
                val todayCount = dao.getPendingTasksForDay(startOfDay, endOfDay).first().size
                val overdueCount = dao.getOverdueTasks(now).first().size

                // Dispatch via NotificationHelper (respects quiet hours)
                NotificationHelper.sendMorningBriefing(context, todayCount, overdueCount)

                // Reschedule for tomorrow
                val prefs = RoutinePreferences(context)
                val (hour, minute) = prefs.planningTime.first()
                AlarmScheduler.schedule(context, hour, minute)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
