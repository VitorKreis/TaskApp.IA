package com.example.myapplication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
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

    private lateinit var etTitle            : TextInputEditText
    private lateinit var etDescription      : TextInputEditText
    private lateinit var spinnerPriorityAuto: AutoCompleteTextView
    private lateinit var tvSelectedDate     : TextView
    private lateinit var btnPickDate        : MaterialButton
    private lateinit var btnClearDate       : MaterialButton
    private lateinit var btnSave            : MaterialButton

    private var editingTaskId  : Long? = null
    private var selectedDueDate: Long? = null
    private var selectedPriority: Int = 1 // Média padrão

    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val priorities = listOf("Baixa", "Média", "Alta", "Urgente")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_task)

        // Toolbar
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Views
        etTitle             = findViewById(R.id.et_title)
        etDescription       = findViewById(R.id.et_description)
        spinnerPriorityAuto = findViewById(R.id.spinner_priority_auto)
        tvSelectedDate      = findViewById(R.id.tv_selected_date)
        btnPickDate         = findViewById(R.id.btn_pick_date)
        btnClearDate        = findViewById(R.id.btn_clear_date)
        btnSave             = findViewById(R.id.btn_save)

        // Modern Exposed Dropdown (Prioridades)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, priorities)
        spinnerPriorityAuto.setAdapter(adapter)
        spinnerPriorityAuto.setText(priorities[selectedPriority], false)
        spinnerPriorityAuto.setOnItemClickListener { _, _, position, _ ->
            selectedPriority = position
        }

        // Date picker
        btnPickDate.setOnClickListener { showDateTimePicker() }
        btnClearDate.setOnClickListener {
            selectedDueDate = null
            tvSelectedDate.text = "Sem prazo definido"
            btnClearDate.visibility = View.GONE
        }

        // Salvar
        btnSave.setOnClickListener { saveTask() }

        // Carregar tarefa se for edição
        val id = intent.getLongExtra("EXTRA_TASK_ID", -1L)
        if (id != -1L) loadTask(id)
    }

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

    private fun saveTask() {
        val title = etTitle.text?.toString()?.trim().orEmpty()
        if (title.isEmpty()) {
            etTitle.error = "Título obrigatório"
            return
        }

        setResult(RESULT_OK, Intent().apply {
            putExtra("title",       title)
            putExtra("description", etDescription.text?.toString()?.trim().orEmpty())
            putExtra("priority",    selectedPriority)
            putExtra("dueDate",     selectedDueDate ?: -1L)
            putExtra("taskId",      editingTaskId   ?: -1L)
        })
        finish()
    }

    private fun loadTask(id: Long) {
        editingTaskId = id
        // Placeholder até criarmos o Room / Repository
        // val task = TaskStore.findById(id) ?: return
        
        supportActionBar?.title = "Editar Tarefa"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
