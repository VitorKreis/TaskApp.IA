package com.example.myapplication.ui.screens.addedittask

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.local.entity.TaskEntity
import com.example.myapplication.presentation.viewmodel.TaskViewModel
import com.example.myapplication.ui.components.DarkDatePickerDialog
import com.example.myapplication.ui.components.DarkTextField
import com.example.myapplication.ui.components.DarkTimePickerDialog
import com.example.myapplication.ui.components.GlassmorphismCard
import com.example.myapplication.ui.components.GradientButton
import com.example.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

private data class PriorityOption(val index: Int, val label: String, val color: Color)

private val priorityOptions = listOf(
    PriorityOption(0, "Baixa", PriorityLow),
    PriorityOption(1, "Media", PriorityMedium),
    PriorityOption(2, "Alta", PriorityHigh),
    PriorityOption(3, "Urgente", PriorityUrgent)
)

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
    var dateError by remember { mutableStateOf<String?>(null) }
    var loaded by rememberSaveable { mutableStateOf(false) }

    // Picker dialog states
    var showDueDatePicker by remember { mutableStateOf(false) }
    var showDueTimePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var tempDate by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(taskId) {
        if (isEditing && !loaded) {
            // Preenche o formulário uma única vez ao abrir em modo edição.
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

    fun validateDates(): Boolean {
        val now = System.currentTimeMillis()
        if (!isEditing) {
            // Na criação, bloqueia datas passadas para evitar tarefas já vencidas ao nascer.
            if (dueDate > 0 && dueDate < now) {
                dateError = "O prazo nao pode ser no passado"
                return false
            }
            if (startTime > 0 && startTime < now) {
                dateError = "O horario de inicio nao pode ser no passado"
                return false
            }
        }
        if (startTime > 0 && endTime > 0 && endTime <= startTime) {
            dateError = "O fim deve ser depois do inicio"
            return false
        }
        dateError = null
        return true
    }

    val today = LocalDate.now()
    val minPickerDate = if (isEditing) null else today

    // -- Date/Time Picker Dialogs --
    if (showDueDatePicker) {
        val initial = if (dueDate > 0) Instant.ofEpochMilli(dueDate).atZone(ZoneId.systemDefault()).toLocalDate() else today
        DarkDatePickerDialog(
            initialDate = initial,
            minDate = minPickerDate,
            onDateSelected = { date -> tempDate = date; showDueDatePicker = false; showDueTimePicker = true },
            onDismiss = { showDueDatePicker = false }
        )
    }
    if (showDueTimePicker) {
        val cal = Calendar.getInstance().apply { if (dueDate > 0) timeInMillis = dueDate }
        DarkTimePickerDialog(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            onTimeSelected = { h, m ->
                val c = Calendar.getInstance().apply {
                    set(tempDate.year, tempDate.monthValue - 1, tempDate.dayOfMonth, h, m, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                dueDate = c.timeInMillis; dateError = null
            },
            onDismiss = { showDueTimePicker = false }
        )
    }
    if (showStartDatePicker) {
        val initial = if (startTime > 0) Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()).toLocalDate() else today
        DarkDatePickerDialog(
            initialDate = initial,
            minDate = minPickerDate,
            onDateSelected = { date -> tempDate = date; showStartDatePicker = false; showStartTimePicker = true },
            onDismiss = { showStartDatePicker = false }
        )
    }
    if (showStartTimePicker) {
        val cal = Calendar.getInstance().apply { if (startTime > 0) timeInMillis = startTime }
        DarkTimePickerDialog(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            onTimeSelected = { h, m ->
                val c = Calendar.getInstance().apply {
                    set(tempDate.year, tempDate.monthValue - 1, tempDate.dayOfMonth, h, m, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                startTime = c.timeInMillis; dateError = null
            },
            onDismiss = { showStartTimePicker = false }
        )
    }
    if (showEndDatePicker) {
        val initial = if (endTime > 0) Instant.ofEpochMilli(endTime).atZone(ZoneId.systemDefault()).toLocalDate()
            else if (startTime > 0) Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()).toLocalDate()
            else today
        DarkDatePickerDialog(
            initialDate = initial,
            minDate = minPickerDate,
            onDateSelected = { date -> tempDate = date; showEndDatePicker = false; showEndTimePicker = true },
            onDismiss = { showEndDatePicker = false }
        )
    }
    if (showEndTimePicker) {
        val cal = Calendar.getInstance().apply { if (endTime > 0) timeInMillis = endTime }
        DarkTimePickerDialog(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            onTimeSelected = { h, m ->
                val c = Calendar.getInstance().apply {
                    set(tempDate.year, tempDate.monthValue - 1, tempDate.dayOfMonth, h, m, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                endTime = c.timeInMillis; dateError = null
            },
            onDismiss = { showEndTimePicker = false }
        )
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
            Spacer(Modifier.height(4.dp))

            // -- Title --
            DarkTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = "O que precisa ser feito? *",
                isError = titleError,
                supportingText = if (titleError) {
                    { Text("Titulo obrigatorio", color = OverdueRed) }
                } else null
            )

            // -- Description --
            DarkTextField(
                value = description,
                onValueChange = { description = it },
                label = "Descricao (opcional)",
                singleLine = false,
                minLines = 3,
                maxLines = 5
            )

            // -- Priority Cards --
            Column {
                Text(
                    "Prioridade",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    priorityOptions.forEach { option ->
                        val isSelected = priority == option.index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .then(
                                    if (isSelected) Modifier.background(option.color.copy(alpha = 0.2f))
                                    else Modifier.background(SurfaceDark)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) option.color else GlassBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { priority = option.index },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(option.color)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    option.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) option.color else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // -- Due Date --
            GlassmorphismCard {
                Column {
                    Text("Prazo", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, tint = DarkGreen, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (dueDate > 0) sdf.format(Date(dueDate)) else "Sem prazo definido",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (dueDate > 0) TextPrimary else TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        GradientButton(
                            text = if (dueDate > 0) "Alterar" else "Definir",
                            onClick = { showDueDatePicker = true },
                            modifier = Modifier.width(100.dp)
                        )
                    }
                    if (dueDate > 0) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { dueDate = -1L; dateError = null }
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Clear, "Limpar", tint = OverdueRed, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Remover prazo", style = MaterialTheme.typography.labelMedium, color = OverdueRed)
                        }
                    }
                }
            }

            // -- Time Range --
            GlassmorphismCard {
                Column {
                    Text("Horario (Evento)", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Defina inicio e fim para criar um evento com duracao",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Schedule, null, tint = DarkGreen, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Inicio", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Text(
                                text = if (startTime > 0) sdf.format(Date(startTime)) else "Nao definido",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (startTime > 0) TextPrimary else TextSecondary
                            )
                        }
                        GradientButton(text = "Definir", onClick = { showStartDatePicker = true }, modifier = Modifier.width(100.dp))
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.AccessTime, null, tint = Purple, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fim", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Text(
                                text = if (endTime > 0) sdf.format(Date(endTime)) else "Nao definido",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (endTime > 0) TextPrimary else TextSecondary
                            )
                        }
                        GradientButton(text = "Definir", onClick = { showEndDatePicker = true }, modifier = Modifier.width(100.dp))
                    }

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
                            Text("Duracao: $durationText", style = MaterialTheme.typography.labelLarge, color = SuccessGreen)
                        }
                    }

                    if (startTime > 0 || endTime > 0) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { startTime = -1L; endTime = -1L; dateError = null }
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Clear, "Limpar", tint = OverdueRed, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Remover horario", style = MaterialTheme.typography.labelMedium, color = OverdueRed)
                        }
                    }
                }
            }

            // -- Date Error --
            if (dateError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(OverdueRed.copy(alpha = 0.1f))
                        .border(1.dp, OverdueRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text("$dateError", style = MaterialTheme.typography.bodyMedium, color = OverdueRed)
                }
            }

            // -- Save Button --
            GradientButton(
                text = if (isEditing) "Salvar Alteracoes" else "Criar Tarefa",
                onClick = {
                    if (title.isBlank()) { titleError = true; return@GradientButton }
                    if (!validateDates()) return@GradientButton

                    // Converte valores "não definidos" (-1) para null antes de persistir no Room.
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
