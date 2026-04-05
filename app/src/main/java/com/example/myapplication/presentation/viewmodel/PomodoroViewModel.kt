package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.TaskEntity
import com.example.myapplication.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Preset de duração Pomodoro. */
enum class PomodoroPreset(
    val label: String,
    val focusMinutes: Int,
    val breakMinutes: Int
) {
    CLASSIC("Clássico", 25, 5),
    LONG("Longo", 50, 10)
}

/** Fase atual do ciclo. */
enum class PomodoroPhase { IDLE, FOCUS, BREAK, FINISHED }

data class PomodoroState(
    val preset: PomodoroPreset = PomodoroPreset.CLASSIC,
    val phase: PomodoroPhase = PomodoroPhase.IDLE,
    val remainingSeconds: Int = PomodoroPreset.CLASSIC.focusMinutes * 60,
    val completedCycles: Int = 0,
    val isRunning: Boolean = false,
    val taskName: String = "",
    /** true quando o timer de foco zerou e aguarda o usuário iniciar a pausa. */
    val awaitingBreakStart: Boolean = false
)

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PomodoroState())
    val state: StateFlow<PomodoroState> = _state.asStateFlow()

    private var timerJob: Job? = null

    // ── Configuração ────────────────────────────────────────────────────

    fun setPreset(preset: PomodoroPreset) {
        if (_state.value.phase != PomodoroPhase.IDLE) return
        _state.update {
            it.copy(
                preset = preset,
                remainingSeconds = preset.focusMinutes * 60
            )
        }
    }

    fun setTaskName(name: String) {
        _state.update { it.copy(taskName = name) }
    }

    // ── Controles do Timer ──────────────────────────────────────────────

    fun startFocus() {
        val s = _state.value
        if (s.isRunning) return
        _state.update {
            it.copy(
                phase = PomodoroPhase.FOCUS,
                remainingSeconds = if (s.phase == PomodoroPhase.IDLE || s.awaitingBreakStart)
                    s.preset.focusMinutes * 60 else s.remainingSeconds,
                isRunning = true,
                awaitingBreakStart = false
            )
        }
        startCountdown()
    }

    fun startBreak() {
        val s = _state.value
        if (s.isRunning) return
        _state.update {
            it.copy(
                phase = PomodoroPhase.BREAK,
                remainingSeconds = s.preset.breakMinutes * 60,
                isRunning = true,
                awaitingBreakStart = false
            )
        }
        startCountdown()
    }

    fun pause() {
        timerJob?.cancel()
        timerJob = null
        _state.update { it.copy(isRunning = false) }
    }

    fun resume() {
        if (_state.value.isRunning) return
        if (_state.value.phase == PomodoroPhase.IDLE || _state.value.phase == PomodoroPhase.FINISHED) return
        _state.update { it.copy(isRunning = true) }
        startCountdown()
    }

    fun reset() {
        timerJob?.cancel()
        timerJob = null
        _state.update {
            PomodoroState(
                preset = it.preset,
                taskName = it.taskName
            )
        }
    }

    // ── Finalizar Sessão (salva no Room) ────────────────────────────────

    fun finishSession(onSaved: () -> Unit = {}) {
        timerJob?.cancel()
        timerJob = null
        val s = _state.value
        val totalFocusMinutes = s.completedCycles * s.preset.focusMinutes
        // Se estava em foco, adiciona o tempo parcial já consumido
        val partialMinutes = if (s.phase == PomodoroPhase.FOCUS) {
            val elapsed = s.preset.focusMinutes * 60 - s.remainingSeconds
            elapsed / 60
        } else 0
        val actualMinutes = totalFocusMinutes + partialMinutes

        if (actualMinutes <= 0 && s.completedCycles == 0) {
            // Nada a salvar
            _state.update { PomodoroState() }
            onSaved()
            return
        }

        val name = s.taskName.ifBlank { "Sessão Pomodoro" }
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            repository.insert(
                TaskEntity(
                    title = "🍅 $name",
                    description = "${s.completedCycles} pomodoro(s) • ${s.preset.label}",
                    priority = 0,
                    isDone = true,
                    dueDate = now,
                    actualMinutes = if (actualMinutes > 0) actualMinutes else 1,
                    completedAt = now,
                    tags = listOf("#pomodoro")
                )
            )
            _state.update { PomodoroState() }
            onSaved()
        }
    }

    // ── Timer interno (coroutine-safe) ──────────────────────────────────

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _state.value.remainingSeconds > 0) {
                delay(1000)
                _state.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
            if (isActive) onTimerFinished()
        }
    }

    private fun onTimerFinished() {
        val s = _state.value
        when (s.phase) {
            PomodoroPhase.FOCUS -> {
                // Ciclo de foco concluído
                _state.update {
                    it.copy(
                        completedCycles = it.completedCycles + 1,
                        phase = PomodoroPhase.FOCUS,
                        isRunning = false,
                        awaitingBreakStart = true,
                        remainingSeconds = 0
                    )
                }
            }
            PomodoroPhase.BREAK -> {
                // Pausa concluída — volta ao estado pronto para novo foco
                _state.update {
                    it.copy(
                        phase = PomodoroPhase.IDLE,
                        isRunning = false,
                        remainingSeconds = it.preset.focusMinutes * 60
                    )
                }
            }
            else -> { /* nada */ }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
