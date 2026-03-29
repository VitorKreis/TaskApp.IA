package com.example.myapplication.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import com.example.myapplication.data.local.database.AppDatabase
import com.example.myapplication.data.local.entity.TaskEntity
import com.example.myapplication.data.repository.TaskRepository

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository

    private val allTasksLd: LiveData<List<TaskEntity>>

    val overdueTasks: LiveData<List<TaskEntity>>
    val totalCount: LiveData<Int>
    val doneCount: LiveData<Int>
    val pendingCount: LiveData<Int>
    val overdueCount: LiveData<Int>
    val priorityCounts: LiveData<Map<Int, Int>>

    init {
        val taskDao = AppDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)

        allTasksLd = repository.allTasks.asLiveData()
        overdueTasks = repository.getOverdueTasks(System.currentTimeMillis()).asLiveData()

        totalCount = allTasksLd.map { it.size }

        doneCount = allTasksLd.map { tasks ->
            tasks.count { task -> task.isDone }
        }

        pendingCount = allTasksLd.map { tasks ->
            tasks.count { task -> !task.isDone }
        }

        overdueCount = overdueTasks.map { it.size }

        priorityCounts = allTasksLd.map { tasks ->
            tasks.groupBy { task -> task.priority }.mapValues { entry -> entry.value.size }
        }
    }
}

