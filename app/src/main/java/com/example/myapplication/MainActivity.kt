package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.local.entity.TaskEntity
import com.example.myapplication.presentation.viewmodel.TaskViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter
    private lateinit var tvEmptyState: TextView

    private val addTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val title = data.getStringExtra("title").orEmpty()
            val desc = data.getStringExtra("description").orEmpty()
            val priority = data.getIntExtra("priority", 1)
            val dueDate = data.getLongExtra("dueDate", -1L)
            val taskId = data.getLongExtra("taskId", -1L)
            val isDone = data.getBooleanExtra("isDone", false)

            val task = TaskEntity(
                id = if (taskId == -1L) 0 else taskId,
                title = title,
                description = desc,
                priority = priority,
                isDone = if (taskId == -1L) false else isDone,
                dueDate = if (dueDate == -1L) null else dueDate
            )

            if (taskId == -1L) taskViewModel.insert(task) else taskViewModel.update(task)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvEmptyState = findViewById(R.id.tv_empty_state)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_tasks)
        val fab = findViewById<FloatingActionButton>(R.id.fab_add_task)

        // Setup Adapter
        adapter = TaskAdapter(
            tasks = emptyList(),
            onEdit = { task -> editTask(task) },
            onDelete = { task -> taskViewModel.delete(task) },
            onToggle = { task -> toggleTask(task) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Observer do Banco de Dados
        taskViewModel.allTasks.observe(this) { tasks ->
            updateUI(tasks)
        }

        fab.setOnClickListener {
            addTaskLauncher.launch(Intent(this, AddEditTaskActivity::class.java))
        }
    }

    private fun updateUI(tasks: List<TaskEntity>) {
        if (tasks.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
        } else {
            tvEmptyState.visibility = View.GONE
        }

        adapter.updateTasks(tasks)
    }

    private fun editTask(task: TaskEntity) {
        val intent = Intent(this, AddEditTaskActivity::class.java).apply {
            putExtra("EXTRA_TASK_ID", task.id)
            putExtra("title", task.title)
            putExtra("description", task.description)
            putExtra("priority", task.priority)
            putExtra("dueDate", task.dueDate ?: -1L)
            putExtra("isDone", task.isDone)
        }
        addTaskLauncher.launch(intent)
    }

    private fun toggleTask(task: TaskEntity) {
        val updatedTask = task.copy(isDone = !task.isDone)
        taskViewModel.update(updatedTask)
    }
}
