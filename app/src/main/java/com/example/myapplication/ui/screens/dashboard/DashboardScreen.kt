package com.example.myapplication.ui.screens.dashboard

import android.graphics.Color as AndroidColor
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.entity.TaskEntity
import com.example.myapplication.presentation.viewmodel.DashboardViewModel
import com.example.myapplication.ui.components.GlassmorphismCard
import com.example.myapplication.ui.components.GradientButton
import com.example.myapplication.ui.components.GradientFAB
import com.example.myapplication.ui.components.GradientProgressBar
import com.example.myapplication.ui.components.TaskCard
import com.example.myapplication.ui.theme.*
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToTaskList: (filter: Int) -> Unit,
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (Long) -> Unit
) {
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
    val doneCount by viewModel.doneCount.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()
    val overdueCount by viewModel.overdueCount.collectAsStateWithLifecycle()
    val overdueTasks by viewModel.overdueTasks.collectAsStateWithLifecycle()
    val priorityCounts by viewModel.priorityCounts.collectAsStateWithLifecycle()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

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
            // ── Stats Cards ────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { -40 }
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
                                onClick = { onNavigateToTaskList(1) }, // ALL
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Pendentes",
                                value = pendingCount.toString(),
                                icon = Icons.Default.PendingActions,
                                gradient = Brush.linearGradient(listOf(Purple, PurpleBright)),
                                onClick = { onNavigateToTaskList(0) }, // ACTIVE
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
                                onClick = { onNavigateToTaskList(2) }, // DONE
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Atrasadas",
                                value = overdueCount.toString(),
                                icon = Icons.Default.Warning,
                                gradient = Brush.linearGradient(listOf(OverdueRed, PriorityHigh)),
                                onClick = { onNavigateToTaskList(3) }, // OVERDUE
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
                            Text("Progresso", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
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

            // ── Overdue Section ────────────────────────────────────────
            item {
                Text(
                    "Tarefas Atrasadas",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }

            if (overdueTasks.isEmpty()) {
                item {
                    GlassmorphismCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Nenhuma tarefa atrasada 🎉",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SuccessGreen
                        )
                    }
                }
            } else {
                items(overdueTasks, key = { it.id }) { task ->
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
