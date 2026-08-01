package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.ui.theme.QuadrantNotUrgentImportant
import com.example.ui.theme.QuadrantUrgentImportant
import com.example.ui.theme.QuadrantUrgentNotImportant
import com.example.ui.theme.SecondaryEmerald

@Composable
fun EisenhowerMatrixScreen(
    tasks: List<TaskEntity>,
    onToggleTaskCompletion: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onStartFocusSession: (TaskEntity) -> Unit,
    onQuickAddInQuadrant: (isUrgent: Boolean, isImportant: Boolean) -> Unit
) {
    val activeTasks = remember(tasks) { tasks.filter { !it.isCompleted } }

    val q1 = remember(activeTasks) { activeTasks.filter { it.isUrgent && it.isImportant } }
    val q2 = remember(activeTasks) { activeTasks.filter { !it.isUrgent && it.isImportant } }
    val q3 = remember(activeTasks) { activeTasks.filter { it.isUrgent && !it.isImportant } }
    val q4 = remember(activeTasks) { activeTasks.filter { !it.isUrgent && !it.isImportant } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Quadrant 1: DO FIRST
            QuadrantCard(
                title = "1. DO FIRST",
                subtitle = "Urgent & Important",
                color = QuadrantUrgentImportant,
                tasks = q1,
                onToggleTaskCompletion = onToggleTaskCompletion,
                onEditTask = onEditTask,
                onDeleteTask = onDeleteTask,
                onStartFocusSession = onStartFocusSession,
                onAddClick = { onQuickAddInQuadrant(true, true) },
                modifier = Modifier.weight(1f)
            )

            // Quadrant 2: SCHEDULE
            QuadrantCard(
                title = "2. SCHEDULE",
                subtitle = "Not Urgent & Important",
                color = QuadrantNotUrgentImportant,
                tasks = q2,
                onToggleTaskCompletion = onToggleTaskCompletion,
                onEditTask = onEditTask,
                onDeleteTask = onDeleteTask,
                onStartFocusSession = onStartFocusSession,
                onAddClick = { onQuickAddInQuadrant(false, true) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Quadrant 3: DELEGATE
            QuadrantCard(
                title = "3. DELEGATE",
                subtitle = "Urgent & Not Important",
                color = QuadrantUrgentNotImportant,
                tasks = q3,
                onToggleTaskCompletion = onToggleTaskCompletion,
                onEditTask = onEditTask,
                onDeleteTask = onDeleteTask,
                onStartFocusSession = onStartFocusSession,
                onAddClick = { onQuickAddInQuadrant(true, false) },
                modifier = Modifier.weight(1f)
            )

            // Quadrant 4: DON'T DO / ELIMINATE
            QuadrantCard(
                title = "4. DON'T DO",
                subtitle = "Not Urgent & Not Important",
                color = SecondaryEmerald,
                tasks = q4,
                onToggleTaskCompletion = onToggleTaskCompletion,
                onEditTask = onEditTask,
                onDeleteTask = onDeleteTask,
                onStartFocusSession = onStartFocusSession,
                onAddClick = { onQuickAddInQuadrant(false, false) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun QuadrantCard(
    title: String,
    subtitle: String,
    color: Color,
    tasks: List<TaskEntity>,
    onToggleTaskCompletion: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onStartFocusSession: (TaskEntity) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add to Quadrant", tint = color, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Empty quadrant",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onToggleCompletion = onToggleTaskCompletion,
                            onEditTask = onEditTask,
                            onDeleteTask = onDeleteTask,
                            onStartFocusSession = onStartFocusSession
                        )
                    }
                }
            }
        }
    }
}
