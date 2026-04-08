package com.example.myapplication.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.local.preferences.RoutinePreferences
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * Utilitário centralizado para envio de notificações.
 *
 * Antes de disparar qualquer notificação, verifica se o horário atual
 * está dentro da janela de silêncio (quiet hours) configurada pelo usuário.
 * Se estiver, a notificação é silenciosamente suprimida.
 */
object NotificationHelper {

    private const val MORNING_CHANNEL_ID = "morning_notification"
    private const val MORNING_NOTIFICATION_ID = 2001

    /**
     * Inicializa os canais de notificação. Chamar no Application ou quando necessário.
     */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val morningChannel = NotificationChannel(
                MORNING_CHANNEL_ID,
                "Resumo Matinal",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Resumo diário das suas tarefas ao acordar"
            }
            manager.createNotificationChannel(morningChannel)
        }
    }

    /**
     * Verifica se o horário atual está dentro da janela de silêncio do usuário.
     * Deve ser chamada em contexto de coroutine (suspend).
     */
    suspend fun isInQuietHours(context: Context): Boolean {
        val prefs = RoutinePreferences(context)
        val quietHours = prefs.quietHours.first()

        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)

        return quietHours.isInQuietWindow(currentHour, currentMinute)
    }

    /**
     * Envia o Morning Briefing — resumo matinal de tarefas.
     *
     * Respeita a janela de silêncio: se o horário atual estiver dentro
     * do quiet hours, a notificação é suprimida e o método retorna false.
     *
     * @return true se a notificação foi enviada, false se foi suprimida.
     */
    suspend fun sendMorningBriefing(
        context: Context,
        todayCount: Int,
        overdueCount: Int
    ): Boolean {
        // ── Quiet Hours guard ───────────────────────────────────────────
        if (isInQuietHours(context)) return false

        val totalPending = todayCount + overdueCount

        val message = when {
            totalPending == 0 ->
                "Bom dia! Nenhuma tarefa pendente. Aproveite para planejar seu dia! ✨"
            overdueCount > 0 && todayCount > 0 ->
                "Bom dia! Você tem $todayCount tarefa(s) para hoje e $overdueCount atrasada(s). Vamos planejar? 📋"
            overdueCount > 0 ->
                "Bom dia! Você tem $overdueCount tarefa(s) atrasada(s). Hora de resolver! ⚠️"
            else ->
                "Bom dia! Você tem $todayCount tarefa(s) para hoje. Vamos planejar? 🚀"
        }

        showNotification(
            context = context,
            channelId = MORNING_CHANNEL_ID,
            notificationId = MORNING_NOTIFICATION_ID,
            title = "TaskApp.IA ☀️",
            message = message
        )
        return true
    }

    /**
     * Dispara uma notificação genérica respeitando o quiet hours.
     *
     * @return true se a notificação foi enviada, false se suprimida.
     */
    suspend fun sendNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String
    ): Boolean {
        if (isInQuietHours(context)) return false

        showNotification(context, channelId, notificationId, title, message)
        return true
    }

    // ── Private ─────────────────────────────────────────────────────────

    private fun showNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String
    ) {
        createChannels(context)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId, notification)
    }
}
