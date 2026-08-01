package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long?,
    val taskTitle: String?,
    val durationMinutes: Int = 25,
    val completedAt: Long = System.currentTimeMillis()
)
