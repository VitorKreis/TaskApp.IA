package com.example.myapplication.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.local.entity.TaskEntity
import com.example.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

@Composable
fun TaskCard(
    task: TaskEntity,
    onToggle: (TaskEntity) -> Unit,
    onEdit: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (task.priority) {
        0 -> PriorityLow
        1 -> PriorityMedium
        2 -> PriorityHigh
        3 -> PriorityUrgent
        else -> PriorityMedium
    }
    val priorityLabel = when (task.priority) {
        0 -> "Baixa"
        1 -> "Média"
        2 -> "Alta"
        3 -> "Urgente"
        else -> "Média"
    }
    val isOverdue = !task.isDone && task.dueDate != null && task.dueDate < System.currentTimeMillis()

    val borderColor by animateColorAsState(
        targetValue = if (isOverdue) OverdueRed.copy(alpha = 0.6f)
        else priorityColor.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "border"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .alpha(if (task.isDone) 0.6f else 1f)
            .clickable { onEdit(task) }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Priority indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(priorityColor)
            )

            Spacer(Modifier.width(12.dp))

            // Checkbox
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { onToggle(task) },
                colors = CheckboxDefaults.colors(
                    checkedColor = Purple,
                    uncheckedColor = TextSecondary,
                    checkmarkColor = TextPrimary
                )
            )

            Spacer(Modifier.width(8.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                )

                if (task.description.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Priority badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(priorityColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = priorityLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = priorityColor
                        )
                    }

                    // Due date
                    if (task.dueDate != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = if (isOverdue) OverdueRed else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = sdf.format(Date(task.dueDate)),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isOverdue) OverdueRed else TextSecondary
                            )
                        }
                    }

                    // Time range (evento)
                    if (task.startTime != null && task.endTime != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${timeFmt.format(Date(task.startTime))} – ${timeFmt.format(Date(task.endTime))}",
                                style = MaterialTheme.typography.labelMedium,
                                color = DarkGreen
                            )
                        }
                    }
                }
            }

            // Action buttons
            Column {
                IconButton(onClick = { onEdit(task) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, "Editar", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { onDelete(task) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, "Excluir", tint = OverdueRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
