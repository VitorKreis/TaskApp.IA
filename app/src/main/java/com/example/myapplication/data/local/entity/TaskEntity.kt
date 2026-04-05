package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val priority: Int,
    val isDone: Boolean,
    val dueDate: Long?,
    val startTime: Long? = null,
    val endTime: Long? = null,
    // Feature 1: Focus Timer
    val estimatedMinutes: Int? = null,
    val actualMinutes: Int = 0,
    // Feature 2: Snooze Tracker
    val postponedCount: Int = 0,
    // Feature 3: Effort Feedback
    val energyLevel: Int? = null,
    val completedAt: Long? = null,
    // Feature 4: Tags
    val tags: List<String> = emptyList()
)
