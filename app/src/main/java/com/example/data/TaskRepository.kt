package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val focusSessionDao: FocusSessionDao
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allFocusSessions: Flow<List<FocusSessionEntity>> = focusSessionDao.getAllSessions()

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun toggleTaskCompletion(task: TaskEntity): Boolean {
        val newCompleted = !task.isCompleted
        val completedAt = if (newCompleted) System.currentTimeMillis() else null
        val status = if (newCompleted) "DONE" else "TODO"
        taskDao.updateTaskCompletion(task.id, newCompleted, completedAt, status)
        return newCompleted
    }

    suspend fun insertFocusSession(session: FocusSessionEntity) {
        focusSessionDao.insertSession(session)
        session.taskId?.let { taskId ->
            val task = taskDao.getTaskById(taskId)
            task?.let {
                val updated = it.copy(completedPomodoros = it.completedPomodoros + 1)
                taskDao.updateTask(updated)
            }
        }
    }
}
