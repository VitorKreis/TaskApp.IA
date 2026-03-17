package com.example.myapplication

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Task(
    val id: Long = nextId++,
    var title: String,
    var description: String = "",
    var priority: Priority = Priority.MEDIUM,
    var status: Status = Status.PENDING,
    var dueDate: Long? = null
) {
    enum class Priority { LOW, MEDIUM, HIGH, URGENT }
    enum class Status   { PENDING, DONE }

    val isDone get() = status == Status.DONE

    val isOverdue get() = dueDate != null && !isDone && dueDate!! < System.currentTimeMillis()

    fun formattedDueDate(): String? = dueDate?.let {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it))
    }

    fun priorityLabel() = when (priority) {
        Priority.LOW    -> "Baixa"
        Priority.MEDIUM -> "Média"
        Priority.HIGH   -> "Alta"
        Priority.URGENT -> "Urgente"
    }

    fun priorityColor() = when (priority) {
        Priority.LOW    -> 0xFF4CAF50.toInt()
        Priority.MEDIUM -> 0xFFFFC107.toInt()
        Priority.HIGH   -> 0xFFFF5722.toInt()
        Priority.URGENT -> 0xFFE53935.toInt()
    }

    companion object {
        private var nextId = 1L
    }
}