package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FocusSessionEntity
import com.example.data.TaskEntity
import com.example.data.TaskRepository
import com.example.nlp.NlpParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppView {
    LIST,
    EISENHOWER,
    KANBAN,
    FOCUS,
    STATS
}

enum class TaskFilter {
    ALL,
    TODAY,
    UPCOMING,
    HIGH_PRIORITY,
    COMPLETED
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TaskRepository(db.taskDao(), db.focusSessionDao())
    }

    // UI Preferences
    val isDarkMode = MutableStateFlow(true)
    val currentView = MutableStateFlow(AppView.LIST)
    val taskFilter = MutableStateFlow(TaskFilter.ALL)
    val searchQuery = MutableStateFlow("")
    val selectedTagFilter = MutableStateFlow<String?>(null)

    // Modals
    val isCommandPaletteOpen = MutableStateFlow(false)
    val isProfileDialogOpen = MutableStateFlow(false)
    val isAddTaskDialogOpen = MutableStateFlow(false)
    val editingTask = MutableStateFlow<TaskEntity?>(null)

    // Micro-interactions / Gamification
    val confettiTrigger = MutableStateFlow(0L)

    // Focus Mode Timer State
    val activeFocusTask = MutableStateFlow<TaskEntity?>(null)
    val focusTimerSeconds = MutableStateFlow(25 * 60) // 25 minutes default
    val totalFocusTimerDuration = MutableStateFlow(25 * 60)
    val isTimerRunning = MutableStateFlow(false)
    val isTimerPaused = MutableStateFlow(false)
    private var timerEndTimestampMillis: Long? = null
    private var timerJob: Job? = null

    // Raw Tasks from Room
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val focusSessions: StateFlow<List<FocusSessionEntity>> = repository.allFocusSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered Tasks
    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        allTasks,
        searchQuery,
        taskFilter,
        selectedTagFilter
    ) { tasks, query, filter, tag ->
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEnd = todayStart + (24 * 60 * 60 * 1000)

        tasks.filter { task ->
            // Query filter
            val matchesQuery = query.isEmpty() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.tags.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)

            // Category filter
            val matchesCategory = when (filter) {
                TaskFilter.ALL -> !task.isCompleted
                TaskFilter.TODAY -> !task.isCompleted && (task.dueDateTimestamp == null || task.dueDateTimestamp in todayStart..todayEnd)
                TaskFilter.UPCOMING -> !task.isCompleted && (task.dueDateTimestamp != null && task.dueDateTimestamp > todayEnd)
                TaskFilter.HIGH_PRIORITY -> !task.isCompleted && (task.priority == "HIGH" || task.isUrgent)
                TaskFilter.COMPLETED -> task.isCompleted
            }

            // Tag filter
            val matchesTag = tag == null || task.tags.split(",").map { it.trim().lowercase() }.contains(tag.lowercase())

            matchesQuery && matchesCategory && matchesTag
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // All available tags extracted from tasks
    val availableTags: StateFlow<List<String>> = allTasks.combine(MutableStateFlow(Unit)) { tasks, _ ->
        tasks.flatMap { task ->
            task.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }.distinct().sorted()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Streak Calculation
    val streakCount: StateFlow<Int> = allTasks.combine(MutableStateFlow(Unit)) { tasks, _ ->
        calculateStreak(tasks)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // --- Actions ---

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    fun setCurrentView(view: AppView) {
        currentView.value = view
    }

    fun setTaskFilter(filter: TaskFilter) {
        taskFilter.value = filter
        selectedTagFilter.value = null
    }

    fun setTagFilter(tag: String?) {
        selectedTagFilter.value = tag
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun toggleCommandPalette() {
        isCommandPaletteOpen.value = !isCommandPaletteOpen.value
    }

    fun toggleProfileDialog() {
        isProfileDialogOpen.value = !isProfileDialogOpen.value
    }

    fun openAddTaskDialog(taskToEdit: TaskEntity? = null) {
        editingTask.value = taskToEdit
        isAddTaskDialogOpen.value = true
    }

    fun closeAddTaskDialog() {
        isAddTaskDialogOpen.value = false
        editingTask.value = null
    }

    fun createQuickTaskWithNlp(nlpInput: String) {
        if (nlpInput.isBlank()) return
        val parsed = NlpParser.parseInput(nlpInput)
        val entity = NlpParser.toTaskEntity(parsed)
        viewModelScope.launch {
            repository.insertTask(entity)
        }
    }

    fun saveTask(task: TaskEntity) {
        viewModelScope.launch {
            if (task.id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
            }
            closeAddTaskDialog()
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val wasCompleted = repository.toggleTaskCompletion(task)
            if (wasCompleted) {
                confettiTrigger.value = System.currentTimeMillis()
            }
        }
    }

    fun updateTaskStatus(task: TaskEntity, newStatus: String) {
        viewModelScope.launch {
            val isComp = newStatus == "DONE"
            val updated = task.copy(
                status = newStatus,
                isCompleted = isComp,
                completedAt = if (isComp) System.currentTimeMillis() else null
            )
            repository.updateTask(updated)
            if (isComp) {
                confettiTrigger.value = System.currentTimeMillis()
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- Focus Timer Actions ---

    fun startFocusSession(task: TaskEntity? = null, minutes: Int = 25) {
        activeFocusTask.value = task
        val totalSecs = minutes * 60
        totalFocusTimerDuration.value = totalSecs
        focusTimerSeconds.value = totalSecs
        timerEndTimestampMillis = System.currentTimeMillis() + (totalSecs * 1000L)
        isTimerRunning.value = true
        isTimerPaused.value = false

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isTimerRunning.value) {
                delay(500L)
                if (!isTimerPaused.value) {
                    val endMs = timerEndTimestampMillis ?: continue
                    val remaining = maxOf(0, ((endMs - System.currentTimeMillis()) / 1000).toInt())
                    focusTimerSeconds.value = remaining
                    if (remaining <= 0) {
                        onTimerFinished()
                        break
                    }
                }
            }
        }
    }

    fun togglePauseTimer() {
        val currentlyPaused = isTimerPaused.value
        if (!currentlyPaused) {
            // Pausing: evaluate remaining time
            val endMs = timerEndTimestampMillis
            if (endMs != null) {
                val remaining = maxOf(0, ((endMs - System.currentTimeMillis()) / 1000).toInt())
                focusTimerSeconds.value = remaining
            }
            isTimerPaused.value = true
        } else {
            // Resuming: set new target end time
            val remainingSecs = focusTimerSeconds.value
            timerEndTimestampMillis = System.currentTimeMillis() + (remainingSecs * 1000L)
            isTimerPaused.value = false
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        isTimerRunning.value = false
        isTimerPaused.value = false
        timerEndTimestampMillis = null
        focusTimerSeconds.value = totalFocusTimerDuration.value
    }

    private fun onTimerFinished() {
        isTimerRunning.value = false
        isTimerPaused.value = false
        confettiTrigger.value = System.currentTimeMillis()

        viewModelScope.launch {
            val minutes = totalFocusTimerDuration.value / 60
            val session = FocusSessionEntity(
                taskId = activeFocusTask.value?.id,
                taskTitle = activeFocusTask.value?.title ?: "Deep Work Session",
                durationMinutes = minutes
            )
            repository.insertFocusSession(session)
        }
    }

    private fun calculateStreak(tasks: List<TaskEntity>): Int {
        val completedDates = tasks.filter { it.isCompleted && it.completedAt != null }
            .map { task ->
                val cal = Calendar.getInstance().apply { timeInMillis = task.completedAt!! }
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
            }.toSet()

        if (completedDates.isEmpty()) return 0

        var streak = 0
        val cal = Calendar.getInstance()

        while (true) {
            val dateKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
            if (completedDates.contains(dateKey)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                // If today has no completed tasks yet, check if yesterday was completed
                if (streak == 0) {
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    val yesterdayKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
                    if (completedDates.contains(yesterdayKey)) {
                        streak++
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                        continue
                    }
                }
                break
            }
        }
        return streak
    }
}
