package com.example.myapplication.ui.screens.addedittask

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.local.entity.TaskEntity
import com.example.myapplication.presentation.viewmodel.TaskViewModel
import com.example.myapplication.ui.components.DarkTextField
import com.example.myapplication.ui.components.GradientButton
import com.example.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val priorities = listOf("Baixa", "Média", "Alta", "Urgente")
private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
private val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    viewModel: TaskViewModel,
    taskId: Long,
    onNavigateBack: () -> Unit
) {
    val isEditing = taskId > 0L

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable { mutableIntStateOf(1) }
    var dueDate by rememberSaveable { mutableLongStateOf(-1L) }
    var startTime by rememberSaveable { mutableLongStateOf(-1L) }
    var endTime by rememberSaveable { mutableLongStateOf(-1L) }
    var isDone by rememberSaveable { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }
    var loaded by rememberSaveable { mutableStateOf(false) }

    // Load existing task data
    LaunchedEffect(taskId) {
        if (isEditing && !loaded) {
            viewModel.getTaskById(taskId)?.let { task ->
                title = task.title
                description = task.description
                priority = task.priority
                dueDate = task.dueDate ?: -1L
                startTime = task.startTime ?: -1L
                endTime = task.endTime ?: -1L
                isDone = task.isDone
            }
            loaded = true
        }
    }

    val context = LocalContext.current

    fun showDateTimePicker() {
        val cal = Calendar.getInstance().apply {
            if (dueDate > 0) timeInMillis = dueDate
        }
        DatePickerDialog(context, { _, y, m, d ->
            cal.set(y, m, d)
            TimePickerDialog(context, { _, h, min ->
                cal.set(Calendar.HOUR_OF_DAY, h)
                cal.set(Calendar.MINUTE, min)
                cal.set(Calendar.SECOND, 0)
                dueDate = cal.timeInMillis
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    fun showTimePicker(currentValue: Long, onPicked: (Long) -> Unit) {
        val cal = Calendar.getInstance().apply {
            if (currentValue > 0) timeInMillis = currentValue
            else if (dueDate > 0) timeInMillis = dueDate
        }
        DatePickerDialog(context, { _, y, m, d ->
            cal.set(y, m, d)
            TimePickerDialog(context, { _, h, min ->
                cal.set(Calendar.HOUR_OF_DAY, h)
                cal.set(Calendar.MINUTE, min)
                cal.set(Calendar.SECOND, 0)
                onPicked(cal.timeInMillis)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Editar Tarefa" else "Nova Tarefa",
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, "Fechar", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Title ──────────────────────────────────────────────────
            DarkTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = "O que precisa ser feito? *",
                isError = titleError,
                supportingText = if (titleError) {
                    { Text("Título obrigatório", color = OverdueRed) }
                } else null
            )

            // ── Description ────────────────────────────────────────────
            DarkTextField(
                value = description,
                onValueChange = { description = it },
                label = "Descrição (opcional)",
                singleLine = false,
                minLines = 3,
                maxLines = 5
            )

            // ── Priority Dropdown ──────────────────────────────────────
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                DarkTextField(
                    value = priorities[priority],
                    onValueChange = {},
                    label = "Prioridade",
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = SurfaceDark
                ) {
                    priorities.forEachIndexed { index, label ->
                        DropdownMenuItem(
                            text = { Text(label, color = TextPrimary) },
                            onClick = { priority = index; expanded = false },
                            colors = MenuDefaults.itemColors(
                                textColor = TextPrimary
                            )
                        )
                    }
                }
            }

            // ── Date Picker ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .border(1.dp, Purple.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Prazo", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = DarkGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (dueDate > 0) sdf.format(Date(dueDate)) else "Sem prazo definido",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (dueDate > 0) TextPrimary else TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        GradientButton(
                            text = "Alterar",
                            onClick = { showDateTimePicker() },
                            modifier = Modifier.width(100.dp)
                        )
                    }
                    if (dueDate > 0) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { dueDate = -1L }
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Limpar data",
                                tint = OverdueRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Remover prazo", style = MaterialTheme.typography.labelMedium, color = OverdueRed)
                        }
                    }
                }
            }

            // ── Time Range (Evento) ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .border(1.dp, DarkGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Horário (Evento)", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Defina início e fim para criar um evento com duração",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(12.dp))

                    // Start time
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = DarkGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Início", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Text(
                                text = if (startTime > 0) sdf.format(Date(startTime)) else "Não definido",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (startTime > 0) TextPrimary else TextSecondary
                            )
                        }
                        GradientButton(
                            text = "Definir",
                            onClick = { showTimePicker(startTime) { startTime = it } },
                            modifier = Modifier.width(100.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // End time
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Purple,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fim", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Text(
                                text = if (endTime > 0) sdf.format(Date(endTime)) else "Não definido",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (endTime > 0) TextPrimary else TextSecondary
                            )
                        }
                        GradientButton(
                            text = "Definir",
                            onClick = { showTimePicker(endTime) { endTime = it } },
                            modifier = Modifier.width(100.dp)
                        )
                    }

                    // Duration display
                    if (startTime > 0 && endTime > 0 && endTime > startTime) {
                        Spacer(Modifier.height(12.dp))
                        val durationMinutes = (endTime - startTime) / 60000
                        val hours = durationMinutes / 60
                        val minutes = durationMinutes % 60
                        val durationText = when {
                            hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
                            hours > 0 -> "${hours}h"
                            else -> "${minutes}min"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "⏱ Duração: $durationText",
                                style = MaterialTheme.typography.labelLarge,
                                color = SuccessGreen
                            )
                        }
                    }

                    // Clear time
                    if (startTime > 0 || endTime > 0) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { startTime = -1L; endTime = -1L }
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Limpar horário",
                                tint = OverdueRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Remover horário", style = MaterialTheme.typography.labelMedium, color = OverdueRed)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Save Button ────────────────────────────────────────────
            GradientButton(
                text = if (isEditing) "Salvar Alterações" else "Criar Tarefa",
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@GradientButton
                    }
                    val task = TaskEntity(
                        id = if (isEditing) taskId else 0,
                        title = title.trim(),
                        description = description.trim(),
                        priority = priority,
                        isDone = isDone,
                        dueDate = if (dueDate > 0) dueDate else null,
                        startTime = if (startTime > 0) startTime else null,
                        endTime = if (endTime > 0) endTime else null
                    )
                    if (isEditing) viewModel.update(task) else viewModel.insert(task)
                    onNavigateBack()
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
