package com.example.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Subtask
import com.example.data.TaskEntity
import com.example.nlp.NlpParser
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRose
import com.example.ui.theme.SecondaryEmerald
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskDetailDialog(
    isOpen: Boolean,
    taskToEdit: TaskEntity?,
    onDismiss: () -> Unit,
    onSaveTask: (TaskEntity) -> Unit
) {
    if (!isOpen) return

    var nlpInput by remember { mutableStateOf("") }

    var title by remember(taskToEdit) { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember(taskToEdit) { mutableStateOf(taskToEdit?.description ?: "") }
    var priority by remember(taskToEdit) { mutableStateOf(taskToEdit?.priority ?: "MEDIUM") }
    var isUrgent by remember(taskToEdit) { mutableStateOf(taskToEdit?.isUrgent ?: false) }
    var isImportant by remember(taskToEdit) { mutableStateOf(taskToEdit?.isImportant ?: false) }
    var dueDateTimestamp by remember(taskToEdit) { mutableStateOf(taskToEdit?.dueDateTimestamp) }
    var dueTimeString by remember(taskToEdit) { mutableStateOf(taskToEdit?.dueTimeString) }
    var tagsInput by remember(taskToEdit) { mutableStateOf(taskToEdit?.tags ?: "") }

    val subtasksList = remember(taskToEdit) {
        mutableStateListOf<Subtask>().apply {
            if (taskToEdit != null && taskToEdit.subtasksJson.isNotBlank() && taskToEdit.subtasksJson != "[]") {
                try {
                    taskToEdit.subtasksJson.removeSurrounding("[", "]").split(";").forEach { entry ->
                        val parts = entry.split("|")
                        if (parts.size >= 2) {
                            add(Subtask(parts[0], parts[1], parts.getOrNull(2)?.toBoolean() ?: false))
                        }
                    }
                } catch (e: Exception) { }
            }
        }
    }

    var newSubtaskTitle by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Title Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (taskToEdit == null) "New Task" else "Edit Task",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Smart NLP Parser Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Smart Natural Language Input",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = nlpInput,
                            onValueChange = { input ->
                                nlpInput = input
                                if (input.isNotBlank()) {
                                    val parsed = NlpParser.parseInput(input)
                                    title = parsed.title
                                    priority = parsed.priority
                                    isUrgent = parsed.isUrgent
                                    isImportant = parsed.isImportant
                                    if (parsed.dueDateTimestamp != null) dueDateTimestamp = parsed.dueDateTimestamp
                                    if (parsed.dueTimeString != null) dueTimeString = parsed.dueTimeString
                                    if (parsed.tags.isNotEmpty()) tagsInput = parsed.tags.joinToString(",")
                                }
                            },
                            placeholder = { Text("e.g. Buy groceries #shopping !high Friday 5pm") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("nlp_smart_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Task Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Rich Notes / Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Priority Selector
                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val priorities = listOf("HIGH", "MEDIUM", "LOW", "NONE")
                    priorities.forEach { p ->
                        val isSelected = priority == p
                        val pColor = when (p) {
                            "HIGH" -> AccentRose
                            "MEDIUM" -> AccentAmber
                            "LOW" -> SecondaryEmerald
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) pColor else pColor.copy(alpha = 0.12f))
                                .clickable { priority = p }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else pColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Eisenhower Matrix Quadrant Toggles
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Eisenhower Matrix Quadrant",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Urgent", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = isUrgent,
                                onCheckedChange = { isUrgent = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = AccentRose)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Important", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = isImportant,
                                onCheckedChange = { isImportant = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tags Input Field
                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Tags (comma separated, e.g. work, groceries)") },
                    leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subtasks Section
                Text(
                    text = "Subtasks Checklist",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                subtasksList.forEachIndexed { idx, sub ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Checkbox(
                            checked = sub.isDone,
                            onCheckedChange = { checked ->
                                subtasksList[idx] = sub.copy(isDone = checked)
                            }
                        )
                        Text(
                            text = sub.title,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { subtasksList.removeAt(idx) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = AccentRose)
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newSubtaskTitle,
                        onValueChange = { newSubtaskTitle = it },
                        placeholder = { Text("Add a subtask...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newSubtaskTitle.isNotBlank()) {
                                subtasksList.add(Subtask(title = newSubtaskTitle, isDone = false, id = System.currentTimeMillis().toString()))
                                newSubtaskTitle = ""
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Subtask", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val serializedSubtasks = subtasksList.joinToString(";") { "${it.title}|${it.isDone}|${it.id}" }
                                val task = (taskToEdit ?: TaskEntity(title = title)).copy(
                                    title = title.trim(),
                                    description = description.trim(),
                                    priority = priority,
                                    isUrgent = isUrgent,
                                    isImportant = isImportant,
                                    dueDateTimestamp = dueDateTimestamp,
                                    dueTimeString = dueTimeString,
                                    tags = tagsInput.trim(),
                                    subtasksJson = serializedSubtasks
                                )
                                onSaveTask(task)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_task_button")
                    ) {
                        Text("Save Task")
                    }
                }
            }
        }
    }
}
