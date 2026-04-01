package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
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

    @Query("SELECT * FROM tasks WHERE isDone = 0 AND dueDate IS NOT NULL AND dueDate < :currentTime ORDER BY dueDate ASC")
    fun getOverdueTasks(currentTime: Long): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks WHERE
        (dueDate IS NOT NULL AND dueDate >= :startOfDay AND dueDate < :endOfDay)
        OR (startTime IS NOT NULL AND startTime >= :startOfDay AND startTime < :endOfDay)
        ORDER BY COALESCE(startTime, dueDate) ASC
    """)
    fun getTasksForDay(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>
}
