package com.example.myapplication

// Guarda as tarefas em memória enquanto não temos banco.
// Na Fase 3 isso vira Room + Repository.
object TaskStore {

    private val tasks = mutableListOf(
        Task(title = "Estudar Room Database",  description = "Ver docs do Android", priority = Task.Priority.HIGH),
        Task(title = "Fazer compras",          description = "Leite, pão, ovos",    priority = Task.Priority.MEDIUM,
            dueDate = System.currentTimeMillis() + 86_400_000L),
        Task(title = "Responder e-mails",      priority = Task.Priority.LOW),
        Task(title = "Reunião de projeto",     description = "Preparar slides",     priority = Task.Priority.URGENT,
            dueDate = System.currentTimeMillis() + 3_600_000L)
    )

    fun getAll(): MutableList<Task> = tasks

    fun findById(id: Long) = tasks.firstOrNull { it.id == id }

    fun add(task: Task) = tasks.add(0, task)

    fun delete(task: Task) = tasks.remove(task)

    fun insertAt(pos: Int, task: Task) = tasks.add(pos.coerceAtMost(tasks.size), task)
}