package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW", "NONE"
    val isUrgent: Boolean = false,
    val isImportant: Boolean = false,
    val status: String = "TODO", // "TODO", "IN_PROGRESS", "DONE"
    val dueDateTimestamp: Long? = null,
    val dueTimeString: String? = null,
    val tags: String = "", // Comma-separated tags e.g. "work,project"
    val subtasksJson: String = "[]", // Serialized subtasks list
    val estimatedPomodoros: Int = 1,
    val completedPomodoros: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

data class Subtask(
    val id: String,
    val title: String,
    val isDone: Boolean = false
)
