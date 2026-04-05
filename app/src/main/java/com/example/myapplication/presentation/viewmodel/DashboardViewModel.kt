package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.TaskEntity
import com.example.myapplication.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

/** Período do dia para heurística de energia. */
enum class DayPeriod { MORNING, AFTERNOON, EVENING }

/** Dados da saudação inteligente. */
data class SmartGreeting(
    val message: String,
    val suggestedTask: TaskEntity? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repository: TaskRepository
) : ViewModel() {

    private val sub = SharingStarted.WhileSubscribed(5000)

    // ── Helpers de tempo ────────────────────────────────────────────────
    private val now = System.currentTimeMillis()
    private val startOfDay: Long
    private val endOfDay: Long
    private val currentPeriod: DayPeriod

    init {
        val cal = Calendar.getInstance()
        currentPeriod = when (cal.get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> DayPeriod.MORNING
            in 12..17 -> DayPeriod.AFTERNOON
            else -> DayPeriod.EVENING
        }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        startOfDay = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        endOfDay = cal.timeInMillis
    }

    // ── Dados base ──────────────────────────────────────────────────────

    private val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, sub, emptyList())

    val overdueTasks: StateFlow<List<TaskEntity>> =
        repository.getOverdueTasks(now)
            .stateIn(viewModelScope, sub, emptyList())

    val totalCount: StateFlow<Int> = allTasks
        .map { it.size }
        .stateIn(viewModelScope, sub, 0)

    val doneCount: StateFlow<Int> = allTasks
        .map { tasks -> tasks.count { it.isDone } }
        .stateIn(viewModelScope, sub, 0)

    val pendingCount: StateFlow<Int> = allTasks
        .map { tasks -> tasks.count { !it.isDone } }
        .stateIn(viewModelScope, sub, 0)

    val overdueCount: StateFlow<Int> = overdueTasks
        .map { it.size }
        .stateIn(viewModelScope, sub, 0)

    val priorityCounts: StateFlow<Map<Int, Int>> = allTasks
        .map { tasks -> tasks.groupBy { it.priority }.mapValues { it.value.size } }
        .stateIn(viewModelScope, sub, emptyMap())

    // ── Fase 3: Smart Dashboard ─────────────────────────────────────────

    // Mini-métricas do dia
    val pomodoroCountToday: StateFlow<Int> =
        repository.getPomodoroCountToday(startOfDay, endOfDay)
            .stateIn(viewModelScope, sub, 0)

    val totalFocusMinutesToday: StateFlow<Int> =
        repository.getTotalFocusMinutesToday(startOfDay, endOfDay)
            .stateIn(viewModelScope, sub, 0)

    val completedCountToday: StateFlow<Int> =
        repository.getCompletedCountToday(startOfDay, endOfDay)
            .stateIn(viewModelScope, sub, 0)

    // Lista anti-procrastinação
    val procrastinatedTasks: StateFlow<List<TaskEntity>> =
        repository.getProcrastinatedTasks()
            .stateIn(viewModelScope, sub, emptyList())

    // Tarefas pendentes do dia
    val pendingTasksToday: StateFlow<List<TaskEntity>> =
        repository.getPendingTasksForDay(startOfDay, endOfDay)
            .stateIn(viewModelScope, sub, emptyList())

    // Tags disponíveis (derivadas de todas as tarefas)
    val allTags: StateFlow<List<String>> = allTasks
        .map { tasks -> tasks.flatMap { it.tags }.distinct().sorted() }
        .stateIn(viewModelScope, sub, emptyList())

    // ── Heurística de Energia (Saudação Inteligente) ────────────────────

    private val thirtyDaysAgo = now - 30L * 24 * 60 * 60 * 1000

    private val energyHistory: StateFlow<List<TaskEntity>> =
        repository.getCompletedWithEnergySince(thirtyDaysAgo)
            .stateIn(viewModelScope, sub, emptyList())

    private val highPriorityPending: StateFlow<List<TaskEntity>> =
        repository.getTopPendingHighPriority()
            .stateIn(viewModelScope, sub, emptyList())

    val smartGreeting: StateFlow<SmartGreeting> = combine(
        energyHistory,
        highPriorityPending,
        procrastinatedTasks,
        pendingTasksToday
    ) { history, highPri, procrastinated, todayPending ->
        buildGreeting(history, highPri, procrastinated, todayPending)
    }.stateIn(viewModelScope, sub, SmartGreeting(getBaseGreeting()))

    private fun buildGreeting(
        history: List<TaskEntity>,
        highPri: List<TaskEntity>,
        procrastinated: List<TaskEntity>,
        todayPending: List<TaskEntity>
    ): SmartGreeting {
        val baseGreeting = getBaseGreeting()

        // Analisa padrão de energia por período do dia
        val tasksInCurrentPeriod = history.filter { task ->
            val taskCal = Calendar.getInstance().apply { timeInMillis = task.completedAt ?: 0 }
            val taskPeriod = when (taskCal.get(Calendar.HOUR_OF_DAY)) {
                in 5..11 -> DayPeriod.MORNING
                in 12..17 -> DayPeriod.AFTERNOON
                else -> DayPeriod.EVENING
            }
            taskPeriod == currentPeriod
        }

        val avgEnergy = if (tasksInCurrentPeriod.isNotEmpty()) {
            tasksInCurrentPeriod.mapNotNull { it.energyLevel }.average()
        } else 0.0

        // Heurística: energia alta nesse horário (média <= 1.5 = leve = alta energia disponível)
        val isHighEnergyPeriod = tasksInCurrentPeriod.size >= 3 && avgEnergy <= 1.5

        return when {
            isHighEnergyPeriod && highPri.isNotEmpty() -> SmartGreeting(
                message = "$baseGreeting Você costuma ter alta energia agora. Que tal resolver esta?",
                suggestedTask = highPri.first()
            )
            procrastinated.isNotEmpty() -> SmartGreeting(
                message = "$baseGreeting Você tem ${procrastinated.size} tarefa(s) sendo adiada(s). Hora de agir!",
                suggestedTask = procrastinated.first()
            )
            todayPending.isNotEmpty() -> SmartGreeting(
                message = "$baseGreeting Você tem ${todayPending.size} tarefa(s) para hoje. Bora!",
                suggestedTask = todayPending.first()
            )
            else -> SmartGreeting(
                message = "$baseGreeting Nenhuma tarefa urgente agora. Aproveite para planejar!"
            )
        }
    }

    private fun getBaseGreeting(): String {
        return when (currentPeriod) {
            DayPeriod.MORNING -> "Bom dia! ☀️"
            DayPeriod.AFTERNOON -> "Boa tarde! 🌤️"
            DayPeriod.EVENING -> "Boa noite! 🌙"
        }
    }
}

