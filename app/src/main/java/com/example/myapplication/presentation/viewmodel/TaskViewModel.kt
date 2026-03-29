package com.example.myapplication.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.database.AppDatabase
import com.example.myapplication.data.local.entity.TaskEntity
import com.example.myapplication.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

	private val repository: TaskRepository

	val allTasks: LiveData<List<TaskEntity>>

	init {
		val taskDao = AppDatabase.getDatabase(application).taskDao()
		repository = TaskRepository(taskDao)
		allTasks = repository.allTasks.asLiveData()
	}

	fun insert(task: TaskEntity) {
		viewModelScope.launch {
			repository.insert(task)
		}
	}

	fun update(task: TaskEntity) {
		viewModelScope.launch {
			repository.update(task)
		}
	}

	fun delete(task: TaskEntity) {
		viewModelScope.launch {
			repository.delete(task)
		}
	}

	fun getTaskById(id: Long, onResult: (TaskEntity?) -> Unit) {
		viewModelScope.launch {
			onResult(repository.getTaskById(id))
		}
	}
}
