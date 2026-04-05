package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // Ordenação padrão: pendentes primeiro, maior prioridade primeiro e prazo mais próximo no topo.
    @Query("SELECT * FROM tasks ORDER BY isDone ASC, priority DESC, dueDate ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    // Atrasadas = ainda não concluídas, com prazo definido e menor que o horário atual.
    @Query("SELECT * FROM tasks WHERE isDone = 0 AND dueDate IS NOT NULL AND dueDate < :currentTime ORDER BY dueDate ASC")
    fun getOverdueTasks(currentTime: Long): Flow<List<TaskEntity>>

    // Considera tarefa do dia quando o prazo OU início do evento cai no intervalo [startOfDay, endOfDay).
    @Query("""
        SELECT * FROM tasks WHERE
        (dueDate IS NOT NULL AND dueDate >= :startOfDay AND dueDate < :endOfDay)
        OR (startTime IS NOT NULL AND startTime >= :startOfDay AND startTime < :endOfDay)
        ORDER BY COALESCE(startTime, dueDate) ASC
    """)
    fun getTasksForDay(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>

    // ── Fase 3: Smart Dashboard Queries ─────────────────────────────────

    // Soma de actual_minutes das tarefas concluídas hoje
    @Query("SELECT COALESCE(SUM(actualMinutes), 0) FROM tasks WHERE completedAt IS NOT NULL AND completedAt >= :startOfDay AND completedAt < :endOfDay")
    fun getTotalFocusMinutesToday(startOfDay: Long, endOfDay: Long): Flow<Int>

    // Contagem de pomodoros concluídos hoje (tarefas com tag #pomodoro completadas hoje)
    @Query("SELECT COUNT(*) FROM tasks WHERE isDone = 1 AND tags LIKE '%pomodoro%' AND completedAt IS NOT NULL AND completedAt >= :startOfDay AND completedAt < :endOfDay")
    fun getPomodoroCountToday(startOfDay: Long, endOfDay: Long): Flow<Int>

    // Tarefas frequentemente adiadas (postponedCount > 2, não concluídas)
    @Query("SELECT * FROM tasks WHERE isDone = 0 AND postponedCount > 2 ORDER BY postponedCount DESC")
    fun getProcrastinatedTasks(): Flow<List<TaskEntity>>

    // Tarefas concluídas com energy_level agrupadas por período do dia para heurística
    // Retorna tarefas completadas nos últimos 30 dias para análise de padrão de energia
    @Query("SELECT * FROM tasks WHERE isDone = 1 AND energyLevel IS NOT NULL AND completedAt IS NOT NULL AND completedAt >= :since ORDER BY completedAt DESC")
    fun getCompletedWithEnergySince(since: Long): Flow<List<TaskEntity>>

    // Tarefas pendentes de alta prioridade (para sugestão no header)
    @Query("SELECT * FROM tasks WHERE isDone = 0 AND priority >= 2 ORDER BY priority DESC, dueDate ASC LIMIT 5")
    fun getTopPendingHighPriority(): Flow<List<TaskEntity>>

    // Tarefas do dia que ainda não foram concluídas
    @Query("""
        SELECT * FROM tasks WHERE isDone = 0 AND
        ((dueDate IS NOT NULL AND dueDate >= :startOfDay AND dueDate < :endOfDay)
        OR (startTime IS NOT NULL AND startTime >= :startOfDay AND startTime < :endOfDay))
        ORDER BY priority DESC, COALESCE(startTime, dueDate) ASC
    """)
    fun getPendingTasksForDay(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>

    // Total de tarefas concluídas hoje
    @Query("SELECT COUNT(*) FROM tasks WHERE isDone = 1 AND completedAt IS NOT NULL AND completedAt >= :startOfDay AND completedAt < :endOfDay")
    fun getCompletedCountToday(startOfDay: Long, endOfDay: Long): Flow<Int>
}
