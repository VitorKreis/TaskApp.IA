package com.example.myapplication.ui.screens.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bedtime
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.entity.TaskEntity
import com.example.myapplication.presentation.viewmodel.CalendarViewModel
import com.example.myapplication.ui.components.GlassmorphismCard
import com.example.myapplication.ui.components.GradientFAB
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

private val ptMonthNames = mapOf(
    1 to "Janeiro", 2 to "Fevereiro", 3 to "Março", 4 to "Abril",
    5 to "Maio", 6 to "Junho", 7 to "Julho", 8 to "Agosto",
    9 to "Setembro", 10 to "Outubro", 11 to "Novembro", 12 to "Dezembro"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (Long) -> Unit
) {
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val tasksByDay by viewModel.tasksByDay.collectAsStateWithLifecycle()
    val tasksForDay by viewModel.tasksForSelectedDay.collectAsStateWithLifecycle()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendário", color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        floatingActionButton = {
            GradientFAB(onClick = onNavigateToAddTask)
        },
        containerColor = DarkBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Month Header ───────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { -40 }
                ) {
                    GlassmorphismCard {
                        Column {
                            // Month navigation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.previousMonth() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Mês anterior",
                                        tint = TextPrimary
                                    )
                                }
                                Text(
                                    text = "${ptMonthNames[currentMonth.monthValue]} ${currentMonth.year}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { viewModel.nextMonth() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Próximo mês",
                                        tint = TextPrimary
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Day of week headers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val dayLabels = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
                                dayLabels.forEach { label ->
                                    val isWeekend = label == "Sáb" || label == "Dom"
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isWeekend) Purple.copy(alpha = 0.7f) else TextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            // Calendar grid
                            CalendarGrid(
                                yearMonth = currentMonth,
                                selectedDate = selectedDate,
                                tasksByDay = tasksByDay,
                                onDateSelected = { viewModel.selectDate(it) },
                                isWeekend = { viewModel.isWeekend(it) }
                            )
                        }
                    }
                }
            }

            // ── Selected Day Detail ────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { -20 }
                ) {
                    val isWeekend = viewModel.isWeekend(selectedDate)
                    val dayName = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
                        .replaceFirstChar { it.uppercase() }
                    val monthName = ptMonthNames[selectedDate.monthValue] ?: ""

                    Column {
                        Text(
                            text = "$dayName, ${selectedDate.dayOfMonth} de $monthName",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(4.dp))

                        if (tasksForDay.isEmpty() && isWeekend) {
                            // Weekend rest day
                            GlassmorphismCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(listOf(Purple.copy(alpha = 0.3f), DarkGreen.copy(alpha = 0.2f)))
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Bedtime,
                                            contentDescription = null,
                                            tint = Purple,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            "Dia de Descanso 😴",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Purple
                                        )
                                        Text(
                                            "Nenhuma tarefa agendada — aproveite!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        } else if (tasksForDay.isEmpty()) {
                            GlassmorphismCard(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Nenhuma tarefa para este dia",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // ── Tasks for Selected Day ─────────────────────────────────
            items(tasksForDay, key = { it.id }) { task ->
                DayTaskCard(
                    task = task,
                    onClick = { onNavigateToEditTask(task.id) }
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ── Calendar Grid ──────────────────────────────────────────────────────────

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    tasksByDay: Map<Int, List<TaskEntity>>,
    onDateSelected: (LocalDate) -> Unit,
    isWeekend: (LocalDate) -> Boolean
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    // Monday = 1 → offset 0, Sunday = 7 → offset 6
    val startOffset = (firstDayOfMonth.dayOfWeek.value - 1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val totalCells = startOffset + daysInMonth
    val today = LocalDate.now()

    // Rows of 7
    val rows = (totalCells + 6) / 7

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - startOffset + 1

                    if (dayNumber in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayNumber)
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val hasTasks = tasksByDay.containsKey(dayNumber)
                        val weekend = isWeekend(date)

                        CalendarDayCell(
                            day = dayNumber,
                            isSelected = isSelected,
                            isToday = isToday,
                            hasTasks = hasTasks,
                            isWeekend = weekend,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasTasks: Boolean,
    isWeekend: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgModifier = when {
        isSelected -> Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(listOf(DarkGreen, Purple)))
        isToday -> Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, DarkGreen, RoundedCornerShape(10.dp))
        else -> Modifier.clip(RoundedCornerShape(10.dp))
    }

    val textColor = when {
        isSelected -> TextPrimary
        isToday -> DarkGreen
        isWeekend -> Purple.copy(alpha = 0.7f)
        else -> TextPrimary.copy(alpha = 0.9f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .then(bgModifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
            )
            if (hasTasks) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) TextPrimary else SuccessGreen)
                )
            }
        }
    }
}

// ── Day Task Card ──────────────────────────────────────────────────────────

@Composable
private fun DayTaskCard(
    task: TaskEntity,
    onClick: () -> Unit
) {
    val priorityColor = when (task.priority) {
        0 -> PriorityLow
        1 -> PriorityMedium
        2 -> PriorityHigh
        3 -> PriorityUrgent
        else -> PriorityMedium
    }

    val hasTimeRange = task.startTime != null && task.endTime != null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, priorityColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Priority bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(priorityColor)
            )

            Spacer(Modifier.width(12.dp))

            // Time column
            if (hasTimeRange) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(52.dp)
                ) {
                    Text(
                        text = timeFmt.format(Date(task.startTime!!)),
                        style = MaterialTheme.typography.labelLarge,
                        color = DarkGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timeFmt.format(Date(task.endTime!!)),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
                Spacer(Modifier.width(12.dp))
            } else {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
            }

            // Task info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (task.isDone) TextSecondary else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (hasTimeRange) {
                    val durationMin = (task.endTime!! - task.startTime!!) / 60000
                    val h = durationMin / 60
                    val m = durationMin % 60
                    val durText = when {
                        h > 0 && m > 0 -> "${h}h ${m}min"
                        h > 0 -> "${h}h"
                        else -> "${m}min"
                    }
                    Text(
                        text = "⏱ $durText",
                        style = MaterialTheme.typography.labelMedium,
                        color = SuccessGreen
                    )
                }
            }

            // Done indicator
            if (task.isDone) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SuccessGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("✓", style = MaterialTheme.typography.labelMedium, color = SuccessGreen)
                }
            }
        }
    }
}
