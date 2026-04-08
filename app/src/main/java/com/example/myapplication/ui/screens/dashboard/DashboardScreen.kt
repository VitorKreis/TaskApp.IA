package com.example.myapplication.ui.screens.dashboard

import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.entity.TaskEntity
import com.example.myapplication.presentation.viewmodel.DashboardViewModel
import com.example.myapplication.presentation.viewmodel.NotificationViewModel
import com.example.myapplication.ui.components.AnimatedChip
import com.example.myapplication.ui.components.GlassmorphismCard
import com.example.myapplication.ui.components.GradientButton
import com.example.myapplication.ui.components.GradientProgressBar
import com.example.myapplication.ui.components.TaskCard
import com.example.myapplication.ui.components.WakeUpTimeDialog
import com.example.myapplication.ui.theme.*
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    notificationViewModel: NotificationViewModel,
    onNavigateToTaskList: (filter: Int) -> Unit,
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (Long) -> Unit,
    onNavigateToPomodoro: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    // ── Dados existentes ────────────────────────────────────────────────
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
    val doneCount by viewModel.doneCount.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()
    val overdueCount by viewModel.overdueCount.collectAsStateWithLifecycle()
    val overdueTasks by viewModel.overdueTasks.collectAsStateWithLifecycle()
    val priorityCounts by viewModel.priorityCounts.collectAsStateWithLifecycle()

    // ── Dados da Fase 3 ─────────────────────────────────────────────────
    val smartGreeting by viewModel.smartGreeting.collectAsStateWithLifecycle()
    val pomodoroCountToday by viewModel.pomodoroCountToday.collectAsStateWithLifecycle()
    val totalFocusMinutesToday by viewModel.totalFocusMinutesToday.collectAsStateWithLifecycle()
    val completedCountToday by viewModel.completedCountToday.collectAsStateWithLifecycle()
    val procrastinatedTasks by viewModel.procrastinatedTasks.collectAsStateWithLifecycle()
    val pendingTasksToday by viewModel.pendingTasksToday.collectAsStateWithLifecycle()
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()

    var selectedTag by remember { mutableStateOf<String?>(null) }
    var fabExpanded by remember { mutableStateOf(false) }
    var showWakeUpDialog by remember { mutableStateOf(false) }

    val wakeUpTime by notificationViewModel.wakeUpTime.collectAsStateWithLifecycle()

    val filteredTodayTasks by remember(pendingTasksToday, selectedTag) {
        derivedStateOf {
            if (selectedTag == null) pendingTasksToday
            else pendingTasksToday.filter { it.tags.contains(selectedTag) }
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    // ── Wake-Up Time Dialog ─────────────────────────────────────────────
    if (showWakeUpDialog) {
        WakeUpTimeDialog(
            currentHour = wakeUpTime.first,
            currentMinute = wakeUpTime.second,
            onConfirm = { hour, minute ->
                notificationViewModel.setWakeUpTime(hour, minute)
                showWakeUpDialog = false
            },
            onDismiss = { showWakeUpDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "TaskApp.IA",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Rotina e Preferências",
                            tint = TextPrimary
                        )
                    }
                    IconButton(onClick = { showWakeUpDialog = true }) {
                        Icon(
                            Icons.Default.Alarm,
                            contentDescription = "Notificações Matinais",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        floatingActionButton = {
            ExpandableFAB(
                expanded = fabExpanded,
                onToggle = { fabExpanded = !fabExpanded },
                onNewTask = { fabExpanded = false; onNavigateToAddTask() },
                onPomodoro = { fabExpanded = false; onNavigateToPomodoro() }
            )
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
            // ════════════════════════════════════════════════════════════
            // 1) HEADER CONTEXTUAL (Saudação Inteligente)
            // ════════════════════════════════════════════════════════════
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { -40 }
                ) {
                    GlassmorphismCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = smartGreeting.message,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                lineHeight = 24.sp
                            )

                            smartGreeting.suggestedTask?.let { task ->
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(DarkGreen.copy(alpha = 0.15f))
                                        .border(1.dp, DarkGreen.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                        .clickable { onNavigateToEditTask(task.id) }
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("💡", fontSize = 20.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                            val priorityLabel = when (task.priority) {
                                                3 -> "Urgente"
                                                2 -> "Alta"
                                                1 -> "Média"
                                                else -> "Baixa"
                                            }
                                            Text(
                                                text = "Prioridade: $priorityLabel",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary
                                            )
                                        }
                                        Text("→", color = DarkGreen, fontSize = 20.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ════════════════════════════════════════════════════════════
            // 2) MINI-MÉTRICAS DO DIA (Daily Insights)
            // ════════════════════════════════════════════════════════════
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { -30 }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InsightMiniCard(
                            emoji = "🍅",
                            value = pomodoroCountToday.toString(),
                            label = "Pomodoros",
                            modifier = Modifier.weight(1f)
                        )
                        InsightMiniCard(
                            emoji = "⏱",
                            value = "${totalFocusMinutesToday}m",
                            label = "Foco Total",
                            modifier = Modifier.weight(1f)
                        )
                        InsightMiniCard(
                            emoji = "✅",
                            value = completedCountToday.toString(),
                            label = "Concluídas",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ════════════════════════════════════════════════════════════
            // 3) STATS CARDS (mantidos da versão original)
            // ════════════════════════════════════════════════════════════
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { -20 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Total",
                                value = totalCount.toString(),
                                icon = Icons.AutoMirrored.Filled.Assignment,
                                gradient = Brush.linearGradient(listOf(DarkGreen, DarkGreenDeep)),
                                onClick = { onNavigateToTaskList(1) },
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Pendentes",
                                value = pendingCount.toString(),
                                icon = Icons.Default.PendingActions,
                                gradient = Brush.linearGradient(listOf(Purple, PurpleBright)),
                                onClick = { onNavigateToTaskList(0) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Concluídas",
                                value = doneCount.toString(),
                                icon = Icons.Default.CheckCircle,
                                gradient = Brush.linearGradient(listOf(SuccessGreen, DarkGreen)),
                                onClick = { onNavigateToTaskList(2) },
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Atrasadas",
                                value = overdueCount.toString(),
                                icon = Icons.Default.Warning,
                                gradient = Brush.linearGradient(listOf(OverdueRed, PriorityHigh)),
                                onClick = { onNavigateToTaskList(3) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ── Progress ───────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { -20 }
                ) {
                    GlassmorphismCard {
                        Column {
                            Text("Progresso Geral", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Spacer(Modifier.height(8.dp))
                            GradientProgressBar(
                                progress = if (totalCount > 0) doneCount.toFloat() / totalCount else 0f
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${if (totalCount > 0) (doneCount * 100 / totalCount) else 0}% concluído",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // ── PieChart ───────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { -20 }
                ) {
                    GlassmorphismCard {
                        Column {
                            Text(
                                "Distribuição por Prioridade",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(8.dp))
                            PieChartComposable(
                                priorityCounts = priorityCounts,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )
                        }
                    }
                }
            }

            // ════════════════════════════════════════════════════════════
            // 4) LISTA ANTI-PROCRASTINAÇÃO 🚨
            // ════════════════════════════════════════════════════════════
            if (procrastinatedTasks.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Merecem sua Atenção 🚨",
                            style = MaterialTheme.typography.titleLarge,
                            color = PriorityHigh,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                items(procrastinatedTasks, key = { "proc_${it.id}" }) { task ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(listOf(PriorityHigh, PriorityUrgent)),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        TaskCard(
                            task = task,
                            onToggle = {},
                            onEdit = { onNavigateToEditTask(it.id) },
                            onDelete = {}
                        )
                    }
                }
            }

            // ════════════════════════════════════════════════════════════
            // 5) TAREFAS DO DIA + TAG CHIPS
            // ════════════════════════════════════════════════════════════
            item {
                Text(
                    "Tarefas de Hoje",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }

            // Tag filter chips
            if (allTags.isNotEmpty()) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allTags) { tag ->
                            AnimatedChip(
                                label = tag,
                                selected = selectedTag == tag,
                                onClick = {
                                    selectedTag = if (selectedTag == tag) null else tag
                                }
                            )
                        }
                    }
                }
            }

            if (filteredTodayTasks.isEmpty()) {
                item {
                    GlassmorphismCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            if (selectedTag != null) "Nenhuma tarefa com $selectedTag para hoje"
                            else "Nenhuma tarefa pendente para hoje 🎉",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SuccessGreen
                        )
                    }
                }
            } else {
                items(filteredTodayTasks, key = { "today_${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        onToggle = {},
                        onEdit = { onNavigateToEditTask(it.id) },
                        onDelete = {}
                    )
                }
            }

            // ── Overdue Section ────────────────────────────────────────
            if (overdueTasks.isNotEmpty()) {
                item {
                    Text(
                        "Tarefas Atrasadas",
                        style = MaterialTheme.typography.titleLarge,
                        color = OverdueRed
                    )
                }
                items(overdueTasks, key = { "overdue_${it.id}" }) { task ->
                    TaskCard(
                        task = task,
                        onToggle = {},
                        onEdit = { onNavigateToEditTask(it.id) },
                        onDelete = {}
                    )
                }
            }

            // ── View All Button ────────────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                GradientButton(
                    text = "Ver Todas as Tarefas",
                    onClick = { onNavigateToTaskList(1) }
                )
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

// ── Expandable FAB ─────────────────────────────────────────────────────────

@Composable
private fun ExpandableFAB(
    expanded: Boolean,
    onToggle: () -> Unit,
    onNewTask: () -> Unit,
    onPomodoro: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "fab_rotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mini-FABs (visible when expanded)
        AnimatedVisibility(visible = expanded) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.animateContentSize()
            ) {
                // Pomodoro mini-FAB
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .background(SurfaceDark)
                        .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
                        .clickable(onClick = onPomodoro)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        "Foco Rápido",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(PriorityHigh, PurpleBright))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = "Pomodoro",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // New Task mini-FAB
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .background(SurfaceDark)
                        .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
                        .clickable(onClick = onNewTask)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        "Nova Tarefa",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(DarkGreen, Purple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Nova Tarefa",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Main FAB
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        if (expanded) listOf(PriorityHigh, PriorityUrgent)
                        else listOf(DarkGreen, Purple)
                    )
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (expanded) "Fechar" else "Menu",
                tint = TextPrimary,
                modifier = Modifier
                    .size(28.dp)
                    .rotate(rotation)
            )
        }
    }
}

// ── Insight Mini Card ──────────────────────────────────────────────────────

@Composable
private fun InsightMiniCard(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

// ── StatCard ───────────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = TextPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
            Text(title, style = MaterialTheme.typography.labelMedium, color = TextPrimary.copy(alpha = 0.7f))
        }
    }
}

// ── PieChart Wrapper ───────────────────────────────────────────────────────

@Composable
private fun PieChartComposable(
    priorityCounts: Map<Int, Int>,
    modifier: Modifier = Modifier
) {
    val priorityLabels = mapOf(0 to "Baixa", 1 to "Média", 2 to "Alta", 3 to "Urgente")
    val priorityColors = mapOf(
        0 to 0xFF4CAF50.toInt(),
        1 to 0xFFFFC107.toInt(),
        2 to 0xFFFF5722.toInt(),
        3 to 0xFFE53935.toInt()
    )

    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 42f
                transparentCircleRadius = 47f
                setHoleColor(AndroidColor.TRANSPARENT)
                setTransparentCircleColor(AndroidColor.TRANSPARENT)
                setUsePercentValues(false)
                setNoDataText("Nenhuma tarefa ainda")
                setNoDataTextColor(AndroidColor.GRAY)
                legend.isEnabled = true
                legend.textSize = 12f
                legend.textColor = AndroidColor.WHITE
                setEntryLabelColor(AndroidColor.WHITE)
                setEntryLabelTextSize(11f)
            }
        },
        update = { chart ->
            val entries = mutableListOf<PieEntry>()
            val colors = mutableListOf<Int>()

            for (p in 0..3) {
                val count = priorityCounts.getOrDefault(p, 0)
                if (count > 0) {
                    entries.add(PieEntry(count.toFloat(), priorityLabels[p]))
                    colors.add(priorityColors.getValue(p))
                }
            }

            if (entries.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            val dataSet = PieDataSet(entries, "").apply {
                this.colors = colors
                valueTextSize = 13f
                valueTextColor = AndroidColor.WHITE
                sliceSpace = 2f
            }
            chart.data = PieData(dataSet)
            chart.animateY(600)
            chart.invalidate()
        },
        modifier = modifier
    )
}
