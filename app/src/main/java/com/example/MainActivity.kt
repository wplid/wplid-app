package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.TaskEntity
import com.example.ui.AppView
import com.example.ui.MainViewModel
import com.example.ui.components.CommandPaletteDialog
import com.example.ui.components.ConfettiEffect
import com.example.ui.components.ProfileDialog
import com.example.ui.components.TaskDetailDialog
import com.example.util.FeedbackHelper
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.screens.EisenhowerMatrixScreen
import com.example.ui.screens.FocusModeScreen
import com.example.ui.screens.KanbanScreen
import com.example.ui.screens.ListViewScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.FlexTodoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            FlexTodoTheme(darkTheme = isDarkMode) {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainContent(viewModel: MainViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currentView by viewModel.currentView.collectAsState()
    val filteredTasks by viewModel.filteredTasks.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val focusSessions by viewModel.focusSessions.collectAsState()
    val taskFilter by viewModel.taskFilter.collectAsState()
    val selectedTagFilter by viewModel.selectedTagFilter.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()
    val streakCount by viewModel.streakCount.collectAsState()

    val isCommandPaletteOpen by viewModel.isCommandPaletteOpen.collectAsState()
    val isProfileDialogOpen by viewModel.isProfileDialogOpen.collectAsState()
    val isAddTaskDialogOpen by viewModel.isAddTaskDialogOpen.collectAsState()
    val editingTask by viewModel.editingTask.collectAsState()

    val confettiTrigger by viewModel.confettiTrigger.collectAsState()

    // Focus Timer state
    val activeFocusTask by viewModel.activeFocusTask.collectAsState()
    val focusTimerSeconds by viewModel.focusTimerSeconds.collectAsState()
    val totalFocusTimerDuration by viewModel.totalFocusTimerDuration.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val isTimerPaused by viewModel.isTimerPaused.collectAsState()

    val completedCount = remember(allTasks) { allTasks.count { it.isCompleted } }
    val focusMinutesTotal = remember(focusSessions) { focusSessions.sumOf { it.durationMinutes } }

    val context = LocalContext.current
    LaunchedEffect(confettiTrigger) {
        if (confettiTrigger > 0L) {
            val isTimer = (focusTimerSeconds == 0 && !isTimerRunning)
            FeedbackHelper.playCompletionFeedback(context, isTimer = isTimer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            topBar = {
                HeaderTopBar(
                    currentView = currentView,
                    isDarkMode = isDarkMode,
                    streakCount = streakCount,
                    onSwitchView = { viewModel.setCurrentView(it) },
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onOpenCommandPalette = { viewModel.toggleCommandPalette() },
                    onOpenProfile = { viewModel.toggleProfileDialog() }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.openAddTaskDialog(null) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(6.dp),
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("add_task_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(28.dp))
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentView) {
                    AppView.LIST -> {
                        ListViewScreen(
                            tasks = filteredTasks,
                            currentFilter = taskFilter,
                            selectedTag = selectedTagFilter,
                            availableTags = availableTags,
                            onFilterSelect = { viewModel.setTaskFilter(it) },
                            onTagSelect = { viewModel.setTagFilter(it) },
                            onQuickAddNlp = { viewModel.createQuickTaskWithNlp(it) },
                            onToggleTaskCompletion = { viewModel.toggleTaskCompletion(it) },
                            onEditTask = { viewModel.openAddTaskDialog(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            onStartFocusSession = { task ->
                                viewModel.startFocusSession(task)
                                viewModel.setCurrentView(AppView.FOCUS)
                            }
                        )
                    }
                    AppView.EISENHOWER -> {
                        EisenhowerMatrixScreen(
                            tasks = allTasks,
                            onToggleTaskCompletion = { viewModel.toggleTaskCompletion(it) },
                            onEditTask = { viewModel.openAddTaskDialog(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            onStartFocusSession = { task ->
                                viewModel.startFocusSession(task)
                                viewModel.setCurrentView(AppView.FOCUS)
                            },
                            onQuickAddInQuadrant = { isUrgent, isImportant ->
                                val newTask = TaskEntity(
                                    title = "",
                                    isUrgent = isUrgent,
                                    isImportant = isImportant
                                )
                                viewModel.openAddTaskDialog(newTask)
                            }
                        )
                    }
                    AppView.KANBAN -> {
                        KanbanScreen(
                            tasks = allTasks,
                            onToggleTaskCompletion = { viewModel.toggleTaskCompletion(it) },
                            onEditTask = { viewModel.openAddTaskDialog(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            onStartFocusSession = { task ->
                                viewModel.startFocusSession(task)
                                viewModel.setCurrentView(AppView.FOCUS)
                            },
                            onUpdateTaskStatus = { task, status ->
                                viewModel.updateTaskStatus(task, status)
                            }
                        )
                    }
                    AppView.FOCUS -> {
                        FocusModeScreen(
                            activeTask = activeFocusTask,
                            tasks = allTasks,
                            secondsRemaining = focusTimerSeconds,
                            totalDurationSeconds = totalFocusTimerDuration,
                            isRunning = isTimerRunning,
                            isPaused = isTimerPaused,
                            onStartSession = { task, mins -> viewModel.startFocusSession(task, mins) },
                            onTogglePause = { viewModel.togglePauseTimer() },
                            onResetTimer = { viewModel.resetTimer() },
                            onSelectTask = { viewModel.activeFocusTask.value = it }
                        )
                    }
                    AppView.STATS -> {
                        StatsScreen(
                            streakCount = streakCount,
                            tasks = allTasks,
                            focusSessions = focusSessions
                        )
                    }
                }
            }
        }

        // Particle Confetti Effect
        ConfettiEffect(triggerTime = confettiTrigger)

        // Command Palette Modal Dialog
        CommandPaletteDialog(
            isOpen = isCommandPaletteOpen,
            onDismiss = { viewModel.toggleCommandPalette() },
            tasks = allTasks,
            isDarkMode = isDarkMode,
            onToggleDarkMode = { viewModel.toggleDarkMode() },
            onSwitchView = { viewModel.setCurrentView(it) },
            onOpenAddTask = { viewModel.openAddTaskDialog(null) },
            onSelectTask = { viewModel.openAddTaskDialog(it) },
            onQuickCreateNlp = { viewModel.createQuickTaskWithNlp(it) }
        )

        // Add / Edit Task Detail Dialog
        TaskDetailDialog(
            isOpen = isAddTaskDialogOpen,
            taskToEdit = editingTask,
            onDismiss = { viewModel.closeAddTaskDialog() },
            onSaveTask = { viewModel.saveTask(it) }
        )

        // Profile Dialog Modal
        ProfileDialog(
            isOpen = isProfileDialogOpen,
            onDismiss = { viewModel.toggleProfileDialog() },
            streakCount = streakCount,
            completedTasksCount = completedCount,
            totalFocusMinutes = focusMinutesTotal,
            isDarkMode = isDarkMode,
            onToggleDarkMode = { viewModel.toggleDarkMode() }
        )
    }
}

@Composable
fun HeaderTopBar(
    currentView: AppView,
    isDarkMode: Boolean,
    streakCount: Int,
    onSwitchView: (AppView) -> Unit,
    onToggleDarkMode: () -> Unit,
    onOpenCommandPalette: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Title & Brand (Click to open profile)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenProfile() }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon_wplid_glass_1785596074057),
                        contentDescription = "App Logo Profile",
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "wplid",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Streak Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AccentAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = AccentAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$streakCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Search Button (Mobile UI)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onOpenCommandPalette() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("command_palette_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Search",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Theme Toggle
                IconButton(
                    onClick = onToggleDarkMode,
                    modifier = Modifier.testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Dark Mode",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // View Navigation Bar (4 clean tabs: List, Board, Focus, Stats)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ViewNavItem(
                icon = Icons.Default.ListAlt,
                label = "List",
                isSelected = currentView == AppView.LIST,
                onClick = { onSwitchView(AppView.LIST) }
            )
            ViewNavItem(
                icon = Icons.Default.Grid3x3,
                label = "Board",
                isSelected = currentView == AppView.KANBAN,
                onClick = { onSwitchView(AppView.KANBAN) }
            )
            ViewNavItem(
                icon = Icons.Default.Timer,
                label = "Focus",
                isSelected = currentView == AppView.FOCUS,
                onClick = { onSwitchView(AppView.FOCUS) }
            )
            ViewNavItem(
                icon = Icons.Default.ShowChart,
                label = "Stats",
                isSelected = currentView == AppView.STATS,
                onClick = { onSwitchView(AppView.STATS) }
            )
        }
    }
}

@Composable
private fun ViewNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.surface
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
