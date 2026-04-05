package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.TaskEntity
import com.example.myapplication.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Feature 4: Tags — todas as tags únicas extraídas das tarefas ────
    val allTags: StateFlow<List<String>> = allTasks
        .map { tasks -> tasks.flatMap { it.tags }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Feature 1: Focus Timer ──────────────────────────────────────────
    private val _timerTaskId = MutableStateFlow<Long?>(null)
    val timerTaskId: StateFlow<Long?> = _timerTaskId.asStateFlow()

    private val _timerElapsedSeconds = MutableStateFlow(0L)
    val timerElapsedSeconds: StateFlow<Long> = _timerElapsedSeconds.asStateFlow()

    private var timerJob: Job? = null

    fun startTimer(taskId: Long) {
        if (_timerTaskId.value == taskId) return
        stopTimer()
        _timerTaskId.value = taskId
        _timerElapsedSeconds.value = 0
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _timerElapsedSeconds.value++
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        val taskId = _timerTaskId.value ?: return
        val elapsedSeconds = _timerElapsedSeconds.value
        if (elapsedSeconds > 0) {
            val elapsedMinutes = max(1, (elapsedSeconds / 60).toInt())
            viewModelScope.launch {
                repository.getTaskById(taskId)?.let { task ->
                    repository.update(task.copy(actualMinutes = task.actualMinutes + elapsedMinutes))
                }
            }
        }
        _timerTaskId.value = null
        _timerElapsedSeconds.value = 0
    }

    // ── Feature 3: Completar com nível de energia ───────────────────────
    fun completeWithEnergy(task: TaskEntity, energyLevel: Int) {
        viewModelScope.launch {
            repository.update(
                task.copy(
                    isDone = true,
                    energyLevel = energyLevel,
                    completedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun uncomplete(task: TaskEntity) {
        viewModelScope.launch {
            repository.update(
                task.copy(isDone = false, energyLevel = null, completedAt = null)
            )
        }
    }

    // ── Feature 4: Extração de tags do título ───────────────────────────
    fun extractTags(title: String): List<String> {
        return Regex("#\\w+").findAll(title)
            .map { it.value.lowercase() }
            .distinct()
            .toList()
    }

    // ── CRUD padrão ─────────────────────────────────────────────────────
    fun insert(task: TaskEntity) {
        viewModelScope.launch { repository.insert(task) }
    }

    fun update(task: TaskEntity) {
        viewModelScope.launch { repository.update(task) }
    }

    fun delete(task: TaskEntity) {
        viewModelScope.launch { repository.delete(task) }
    }

    suspend fun getTaskById(id: Long): TaskEntity? {
        return repository.getTaskById(id)
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
