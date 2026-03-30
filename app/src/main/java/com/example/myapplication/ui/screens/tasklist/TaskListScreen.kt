package com.example.myapplication.ui.screens.tasklist

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.entity.TaskEntity
import com.example.myapplication.presentation.viewmodel.TaskViewModel
import com.example.myapplication.ui.components.AnimatedChip
import com.example.myapplication.ui.components.GradientFAB
import com.example.myapplication.ui.components.TaskCard
import com.example.myapplication.ui.theme.*

private enum class TaskFilter(val label: String) {
    ACTIVE("Ativas"),
    ALL("Todas"),
    DONE("Concluídas"),
    OVERDUE("Atrasadas")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (Long) -> Unit
) {
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableIntStateOf(0) }
    val now = System.currentTimeMillis()

    val filteredTasks by remember(allTasks, selectedFilter) {
        derivedStateOf {
            when (TaskFilter.entries[selectedFilter]) {
                TaskFilter.ACTIVE -> allTasks.filter { !it.isDone }
                TaskFilter.ALL -> allTasks
                TaskFilter.DONE -> allTasks.filter { it.isDone }
                TaskFilter.OVERDUE -> allTasks.filter {
                    !it.isDone && it.dueDate != null && it.dueDate < now
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tarefas", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        floatingActionButton = {
            GradientFAB(onClick = onNavigateToAddTask)
        },
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Filter Chips ───────────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TaskFilter.entries.size) { index ->
                    AnimatedChip(
                        label = TaskFilter.entries[index].label,
                        selected = selectedFilter == index,
                        onClick = { selectedFilter = index }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Task List ──────────────────────────────────────────────
            Crossfade(
                targetState = filteredTasks.isEmpty(),
                label = "task_content",
                modifier = Modifier.fillMaxSize()
            ) { isEmpty ->
                if (isEmpty) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Nenhuma tarefa encontrada",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                        Text(
                            "Toque no + para começar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onToggle = {
                                    viewModel.update(it.copy(isDone = !it.isDone))
                                },
                                onEdit = { onNavigateToEditTask(it.id) },
                                onDelete = { viewModel.delete(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
