package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.TaskDao
import com.example.myapplication.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getOverdueTasks(currentTime: Long): Flow<List<TaskEntity>> =
        taskDao.getOverdueTasks(currentTime)

    suspend fun insert(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    suspend fun update(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun delete(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun getTaskById(id: Long): TaskEntity? {
        return taskDao.getTaskById(id)
    }
}
