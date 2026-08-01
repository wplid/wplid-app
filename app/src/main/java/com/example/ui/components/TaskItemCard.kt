package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Subtask
import com.example.data.TaskEntity
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRose
import com.example.ui.theme.QuadrantNotUrgentImportant
import com.example.ui.theme.QuadrantUrgentImportant
import com.example.ui.theme.QuadrantUrgentNotImportant
import com.example.ui.theme.SecondaryEmerald
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggleCompletion: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onStartFocusSession: (TaskEntity) -> Unit,
    onUpdateSubtasks: ((TaskEntity, List<Subtask>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var isExpanded by remember { mutableStateOf(false) }

    // Parse subtasks
    val subtasksList = remember(task.subtasksJson) {
        try {
            if (task.subtasksJson.isBlank() || task.subtasksJson == "[]") emptyList()
            else {
                // Simple parsing for subtasks: title|isDone
                task.subtasksJson.removeSurrounding("[", "]").split(";").mapNotNull { entry ->
                    val parts = entry.split("|")
                    if (parts.size >= 2) {
                        Subtask(parts[0], parts[1], parts.getOrNull(2)?.toBoolean() ?: false)
                    } else null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    val priorityColor = when (task.priority.uppercase()) {
        "HIGH" -> AccentRose
        "MEDIUM" -> AccentAmber
        "LOW" -> SecondaryEmerald
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    val cardBg by animateColorAsState(
        targetValue = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(300),
        label = "cardBg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = 1.dp,
                color = if (task.isCompleted) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Custom Checkbox
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (task.isCompleted) SecondaryEmerald else Color.Transparent)
                        .border(
                            2.dp,
                            if (task.isCompleted) SecondaryEmerald else priorityColor,
                            CircleShape
                        )
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onToggleCompletion(task)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Subtitle
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }

                // Focus play button shortcut
                IconButton(
                    onClick = { onStartFocusSession(task) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Focus",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { onEditTask(task) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Task",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { onDeleteTask(task) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Task",
                        tint = AccentRose.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Tags & Meta Row
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Priority Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(priorityColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = task.priority,
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Eisenhower Quadrant Chip
                if (task.isUrgent || task.isImportant) {
                    val quadColor = when {
                        task.isUrgent && task.isImportant -> QuadrantUrgentImportant
                        !task.isUrgent && task.isImportant -> QuadrantNotUrgentImportant
                        task.isUrgent && !task.isImportant -> QuadrantUrgentNotImportant
                        else -> SecondaryEmerald
                    }
                    val quadLabel = when {
                        task.isUrgent && task.isImportant -> "Do First"
                        !task.isUrgent && task.isImportant -> "Schedule"
                        task.isUrgent && !task.isImportant -> "Delegate"
                        else -> "Don't Do"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(quadColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = quadLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = quadColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Due Date Chip
                if (task.dueDateTimestamp != null) {
                    val dateStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(task.dueDateTimestamp))
                    val displayTime = task.dueTimeString ?: ""
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$dateStr $displayTime".trim(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tags Chips
                if (task.tags.isNotBlank()) {
                    task.tags.split(",").forEach { tag ->
                        val cleanTag = tag.trim()
                        if (cleanTag.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "#$cleanTag",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Subtasks toggle section if subtasks exist
            if (subtasksList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                        contentDescription = "Expand Subtasks",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val doneCount = subtasksList.count { it.isDone }
                    Text(
                        text = "Subtasks ($doneCount/${subtasksList.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, top = 4.dp)
                    ) {
                        subtasksList.forEachIndexed { index, sub ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked = sub.isDone,
                                    onCheckedChange = { checked ->
                                        val updatedSubtasks = subtasksList.toMutableList()
                                        updatedSubtasks[index] = sub.copy(isDone = checked)
                                        val serialized = updatedSubtasks.joinToString(";") { "${it.id}|${it.title}|${it.isDone}" }
                                        onUpdateSubtasks?.invoke(task, updatedSubtasks)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = SecondaryEmerald),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sub.title,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = if (sub.isDone) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    color = if (sub.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
