package com.example.myapplication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEditTaskActivity : AppCompatActivity() {

    private lateinit var etTitle        : TextInputEditText
    private lateinit var etDescription  : TextInputEditText
    private lateinit var spinnerPriority: Spinner
    private lateinit var tvSelectedDate : TextView
    private lateinit var btnPickDate    : MaterialButton
    private lateinit var btnClearDate   : MaterialButton
    private lateinit var btnSave        : MaterialButton

    private var editingTaskId  : Long? = null
    private var selectedDueDate: Long? = null

    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_task)

        // Toolbar
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Views
        etTitle         = findViewById(R.id.et_title)
        etDescription   = findViewById(R.id.et_description)
        spinnerPriority = findViewById(R.id.spinner_priority)
        tvSelectedDate  = findViewById(R.id.tv_selected_date)
        btnPickDate     = findViewById(R.id.btn_pick_date)
        btnClearDate    = findViewById(R.id.btn_clear_date)
        btnSave         = findViewById(R.id.btn_save)

        // Spinner
        spinnerPriority.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Baixa", "Média", "Alta", "Urgente")
        )
        spinnerPriority.setSelection(1) // Média padrão

        // Date picker
        btnPickDate.setOnClickListener { showDateTimePicker() }
        btnClearDate.setOnClickListener {
            selectedDueDate = null
            tvSelectedDate.text = "Sem prazo"
            btnClearDate.visibility = View.GONE
        }

        // Salvar
        btnSave.setOnClickListener { saveTask() }

        // Carregar tarefa se for edição
        val id = intent.getLongExtra(MainActivity.EXTRA_TASK_ID, -1L)
        if (id != -1L) loadTask(id)
    }

    // ── Date/time picker ───────────────────────────────────────────────────

    private fun showDateTimePicker() {
        val cal = Calendar.getInstance().apply {
            selectedDueDate?.let { timeInMillis = it }
        }

        DatePickerDialog(this, { _, y, m, d ->
            cal.set(y, m, d)
            TimePickerDialog(this, { _, h, min ->
                cal.set(Calendar.HOUR_OF_DAY, h)
                cal.set(Calendar.MINUTE, min)
                cal.set(Calendar.SECOND, 0)

                selectedDueDate = cal.timeInMillis
                tvSelectedDate.text = sdf.format(Date(selectedDueDate!!))
                btnClearDate.visibility = View.VISIBLE

            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()

        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    // ── Salvar ─────────────────────────────────────────────────────────────

    private fun saveTask() {
        val title = etTitle.text?.toString()?.trim().orEmpty()
        if (title.isEmpty()) {
            etTitle.error = "Título obrigatório"
            return
        }

        setResult(RESULT_OK, Intent().apply {
            putExtra("title",       title)
            putExtra("description", etDescription.text?.toString()?.trim().orEmpty())
            putExtra("priority",    spinnerPriority.selectedItemPosition)
            putExtra("dueDate",     selectedDueDate ?: -1L)
            putExtra("taskId",      editingTaskId   ?: -1L)
        })
        finish()
    }

    // ── Carregar para edição ───────────────────────────────────────────────

    private fun loadTask(id: Long) {
        editingTaskId = id
        val task = TaskStore.findById(id) ?: return

        supportActionBar?.title = "Editar Tarefa"
        etTitle.setText(task.title)
        etDescription.setText(task.description)
        spinnerPriority.setSelection(task.priority.ordinal)

        task.dueDate?.let {
            selectedDueDate = it
            tvSelectedDate.text = sdf.format(Date(it))
            btnClearDate.visibility = View.VISIBLE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}