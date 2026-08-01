package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.TaskEntity
import com.example.ui.components.TaskItemCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryEmerald

@Composable
fun KanbanScreen(
    tasks: List<TaskEntity>,
    onToggleTaskCompletion: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onStartFocusSession: (TaskEntity) -> Unit,
    onUpdateTaskStatus: (TaskEntity, String) -> Unit
) {
    val todoTasks = remember(tasks) { tasks.filter { (it.status == "TODO" || it.status.isBlank()) && !it.isCompleted } }
    val inProgressTasks = remember(tasks) { tasks.filter { it.status == "IN_PROGRESS" && !it.isCompleted } }
    val doneTasks = remember(tasks) { tasks.filter { it.status == "DONE" || it.isCompleted } }

    LazyRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // TO DO Column
        item {
            KanbanColumn(
                title = "To Do",
                status = "TODO",
                badgeColor = PrimaryIndigo,
                tasks = todoTasks,
                nextStatus = "IN_PROGRESS",
                prevStatus = null,
                onToggleTaskCompletion = onToggleTaskCompletion,
                onEditTask = onEditTask,
                onDeleteTask = onDeleteTask,
                onStartFocusSession = onStartFocusSession,
                onUpdateTaskStatus = onUpdateTaskStatus,
                modifier = Modifier.width(280.dp)
            )
        }

        // IN PROGRESS Column
        item {
            KanbanColumn(
                title = "In Progress",
                status = "IN_PROGRESS",
                badgeColor = AccentAmber,
                tasks = inProgressTasks,
                nextStatus = "DONE",
                prevStatus = "TODO",
                onToggleTaskCompletion = onToggleTaskCompletion,
                onEditTask = onEditTask,
                onDeleteTask = onDeleteTask,
                onStartFocusSession = onStartFocusSession,
                onUpdateTaskStatus = onUpdateTaskStatus,
                modifier = Modifier.width(280.dp)
            )
        }

        // DONE Column
        item {
            KanbanColumn(
                title = "Done",
                status = "DONE",
                badgeColor = SecondaryEmerald,
                tasks = doneTasks,
                nextStatus = null,
                prevStatus = "IN_PROGRESS",
                onToggleTaskCompletion = onToggleTaskCompletion,
                onEditTask = onEditTask,
                onDeleteTask = onDeleteTask,
                onStartFocusSession = onStartFocusSession,
                onUpdateTaskStatus = onUpdateTaskStatus,
                modifier = Modifier.width(280.dp)
            )
        }
    }
}

@Composable
private fun KanbanColumn(
    title: String,
    status: String,
    badgeColor: Color,
    tasks: List<TaskEntity>,
    nextStatus: String?,
    prevStatus: String?,
    onToggleTaskCompletion: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onStartFocusSession: (TaskEntity) -> Unit,
    onUpdateTaskStatus: (TaskEntity, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Column Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(badgeColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tasks.size.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks in $title",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        Column {
                            TaskItemCard(
                                task = task,
                                onToggleCompletion = onToggleTaskCompletion,
                                onEditTask = onEditTask,
                                onDeleteTask = onDeleteTask,
                                onStartFocusSession = onStartFocusSession
                            )

                            // Column Movement Controls
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (prevStatus != null) {
                                    IconButton(
                                        onClick = { onUpdateTaskStatus(task, prevStatus) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Move Left",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(24.dp))
                                }

                                if (nextStatus != null) {
                                    IconButton(
                                        onClick = { onUpdateTaskStatus(task, nextStatus) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = "Move Right",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
