package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private val displayTasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var emptyState: android.view.View

    private var currentFilter = "ACTIVE"
    private var currentSearch = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        recycler   = findViewById(R.id.recycler_tasks)
        emptyState = findViewById(R.id.tv_empty_state)

        setupRecyclerView()
        setupChips()
        setupFab()
        applyFilter()
    }

    // ── Setup ──────────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            tasks    = displayTasks,
            onEdit   = { task ->
                startActivityForResult(
                    Intent(this, AddEditTaskActivity::class.java)
                        .putExtra(EXTRA_TASK_ID, task.id),
                    REQUEST_CODE
                )
            },
            onDelete = { task ->
                val pos = TaskStore.getAll().indexOf(task)
                TaskStore.delete(task)
                applyFilter()
                Snackbar.make(recycler, "Tarefa removida", Snackbar.LENGTH_LONG)
                    .setAction("Desfazer") {
                        TaskStore.insertAt(pos, task)
                        applyFilter()
                    }.show()
            },
            onToggle = { task ->
                task.status = if (task.isDone) Task.Status.PENDING else Task.Status.DONE
                applyFilter()
                if (task.isDone)
                    Snackbar.make(recycler, "✓ Concluída!", Snackbar.LENGTH_SHORT).show()
            }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    private fun setupChips() {
        findViewById<ChipGroup>(R.id.chip_group_filter)
            .setOnCheckedStateChangeListener { _, ids ->
                if (ids.isEmpty()) return@setOnCheckedStateChangeListener
                currentFilter = when (ids.first()) {
                    R.id.chip_active  -> "ACTIVE"
                    R.id.chip_all     -> "ALL"
                    R.id.chip_done    -> "DONE"
                    R.id.chip_overdue -> "OVERDUE"
                    else              -> "ACTIVE"
                }
                applyFilter()
            }
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fab_add_task).setOnClickListener {
            startActivityForResult(Intent(this, AddEditTaskActivity::class.java), REQUEST_CODE)
        }
    }

    // ── Filtro ─────────────────────────────────────────────────────────────

    private fun applyFilter() {
        displayTasks.clear()
        displayTasks += TaskStore.getAll()
            .filter { task ->
                when (currentFilter) {
                    "ACTIVE"  -> !task.isDone
                    "DONE"    -> task.isDone
                    "OVERDUE" -> task.isOverdue
                    else      -> true
                }
            }
            .filter { task ->
                currentSearch.isEmpty() ||
                        task.title.contains(currentSearch, ignoreCase = true) ||
                        task.description.contains(currentSearch, ignoreCase = true)
            }

        adapter.notifyDataSetChanged()
        emptyState.visibility = if (displayTasks.isEmpty())
            android.view.View.VISIBLE else android.view.View.GONE
    }

    // ── Resultado do formulário ────────────────────────────────────────────

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE || resultCode != RESULT_OK || data == null) return

        val title    = data.getStringExtra("title") ?: return
        val desc     = data.getStringExtra("description") ?: ""
        val priority = Task.Priority.values()[data.getIntExtra("priority", 1)]
        val dueDate  = data.getLongExtra("dueDate", -1L).takeIf { it != -1L }
        val taskId   = data.getLongExtra("taskId", -1L)

        if (taskId == -1L) {
            TaskStore.add(Task(title = title, description = desc, priority = priority, dueDate = dueDate))
        } else {
            TaskStore.findById(taskId)?.apply {
                this.title       = title
                this.description = desc
                this.priority    = priority
                this.dueDate     = dueDate
            }
        }
        applyFilter()
    }

    // ── Search ─────────────────────────────────────────────────────────────

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val sv = menu.findItem(R.id.action_search).actionView as SearchView
        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(q: String?): Boolean {
                currentSearch = q.orEmpty()
                applyFilter()
                return true
            }
        })
        return true
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val REQUEST_CODE  = 1
    }
}