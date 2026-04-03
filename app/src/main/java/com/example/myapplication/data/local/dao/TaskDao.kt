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
}
