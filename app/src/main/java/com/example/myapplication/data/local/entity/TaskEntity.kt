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
    val endTime: Long? = null
)
