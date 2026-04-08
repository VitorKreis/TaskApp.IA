package com.example.myapplication.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.preferences.PeakFocus
import com.example.myapplication.presentation.viewmodel.RoutineViewModel
import com.example.myapplication.ui.components.GlassmorphismCard
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoutineSettingsScreen(
    viewModel: RoutineViewModel,
    onNavigateBack: () -> Unit
) {
    val planningTime by viewModel.planningTime.collectAsStateWithLifecycle()
    val quietHours by viewModel.quietHours.collectAsStateWithLifecycle()
    val peakFocus by viewModel.peakFocus.collectAsStateWithLifecycle()
    val peakFocusCustomTime by viewModel.peakFocusCustomTime.collectAsStateWithLifecycle()

    // Dialog states
    var showPlanningTimePicker by remember { mutableStateOf(false) }
    var showQuietStartPicker by remember { mutableStateOf(false) }
    var showQuietEndPicker by remember { mutableStateOf(false) }
    var showCustomFocusPicker by remember { mutableStateOf(false) }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // ── Time Picker Dialogs ─────────────────────────────────────────────
    if (showPlanningTimePicker) {
        TimePickerDialog(
            title = "🔔 Horário de Planejamento",
            subtitle = "O app enviará seu resumo matinal neste horário.",
            initialHour = planningTime.first,
            initialMinute = planningTime.second,
            onConfirm = { h, m ->
                viewModel.setPlanningTime(h, m)
                showPlanningTimePicker = false
            },
            onDismiss = { showPlanningTimePicker = false }
        )
    }

    if (showQuietStartPicker) {
        TimePickerDialog(
            title = "🔇 Início do Silêncio",
            subtitle = "Notificações serão suprimidas a partir deste horário.",
            initialHour = quietHours.startHour,
            initialMinute = quietHours.startMinute,
            onConfirm = { h, m ->
                viewModel.setQuietHours(h, m, quietHours.endHour, quietHours.endMinute)
                showQuietStartPicker = false
            },
            onDismiss = { showQuietStartPicker = false }
        )
    }

    if (showQuietEndPicker) {
        TimePickerDialog(
            title = "🔔 Fim do Silêncio",
            subtitle = "Notificações voltam ao normal a partir deste horário.",
            initialHour = quietHours.endHour,
            initialMinute = quietHours.endMinute,
            onConfirm = { h, m ->
                viewModel.setQuietHours(quietHours.startHour, quietHours.startMinute, h, m)
                showQuietEndPicker = false
            },
            onDismiss = { showQuietEndPicker = false }
        )
    }

    if (showCustomFocusPicker) {
        TimePickerDialog(
            title = "⚡ Horário de Pico",
            subtitle = "Defina o horário exato do seu pico de energia.",
            initialHour = peakFocusCustomTime.first,
            initialMinute = peakFocusCustomTime.second,
            onConfirm = { h, m ->
                viewModel.setPeakFocusCustomTime(h, m)
                showCustomFocusPicker = false
            },
            onDismiss = { showCustomFocusPicker = false }
        )
    }

    // ── Screen ──────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Rotina e Preferências",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                },
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
        containerColor = DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── 1. Horário de Planejamento ──────────────────────────────
            AnimatedVisibility(visible, enter = slideInVertically { it / 3 } + fadeIn()) {
                SettingsSection(
                    icon = Icons.Default.Notifications,
                    title = "Horário de Planejamento",
                    description = "O app envia um resumo das suas tarefas do dia neste horário."
                ) {
                    TimeSlotRow(
                        label = "Resumo matinal",
                        hour = planningTime.first,
                        minute = planningTime.second,
                        onClick = { showPlanningTimePicker = true }
                    )
                }
            }

            // ── 2. Janela de Silêncio ───────────────────────────────────
            AnimatedVisibility(visible, enter = slideInVertically { it / 3 } + fadeIn()) {
                SettingsSection(
                    icon = Icons.Default.DoNotDisturb,
                    title = "Janela de Silêncio",
                    description = "Nenhuma notificação será enviada neste intervalo."
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            TimeSlotRow(
                                label = "Início",
                                hour = quietHours.startHour,
                                minute = quietHours.startMinute,
                                onClick = { showQuietStartPicker = true }
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            TimeSlotRow(
                                label = "Fim",
                                hour = quietHours.endHour,
                                minute = quietHours.endMinute,
                                onClick = { showQuietEndPicker = true }
                            )
                        }
                    }
                }
            }

            // ── 3. Pico de Foco ─────────────────────────────────────────
            AnimatedVisibility(visible, enter = slideInVertically { it / 3 } + fadeIn()) {
                SettingsSection(
                    icon = Icons.Default.Bolt,
                    title = "Pico de Foco",
                    description = "Quando você tem mais energia? A IA usará isso para sugerir tarefas."
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PeakFocus.entries.forEach { option ->
                            FilterChip(
                                selected = peakFocus == option,
                                onClick = {
                                    if (option == PeakFocus.CUSTOM) {
                                        showCustomFocusPicker = true
                                    } else {
                                        viewModel.setPeakFocus(option)
                                    }
                                },
                                label = {
                                    val text = if (option == PeakFocus.CUSTOM && peakFocus == PeakFocus.CUSTOM) {
                                        "${option.label} (${formatTime(peakFocusCustomTime.first, peakFocusCustomTime.second)})"
                                    } else {
                                        option.label
                                    }
                                    Text(text)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SurfaceVariantDark,
                                    labelColor = TextSecondary,
                                    selectedContainerColor = DarkGreen.copy(alpha = 0.4f),
                                    selectedLabelColor = TextPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = GlassBorder,
                                    selectedBorderColor = DarkGreen,
                                    enabled = true,
                                    selected = peakFocus == option
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Componentes auxiliares ───────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    icon: ImageVector,
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    GlassmorphismCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun TimeSlotRow(
    label: String,
    hour: Int,
    minute: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariantDark)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccessTime, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                formatTime(hour, minute),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String,
    subtitle: String,
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceDark)
                .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(Modifier.height(16.dp))

            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    clockDialColor = DarkBg,
                    clockDialSelectedContentColor = TextPrimary,
                    clockDialUnselectedContentColor = TextSecondary,
                    selectorColor = DarkGreen,
                    containerColor = SurfaceDark,
                    periodSelectorBorderColor = GlassBorder,
                    periodSelectorSelectedContainerColor = DarkGreen.copy(alpha = 0.3f),
                    periodSelectorUnselectedContainerColor = DarkBg,
                    periodSelectorSelectedContentColor = TextPrimary,
                    periodSelectorUnselectedContentColor = TextSecondary,
                    timeSelectorSelectedContainerColor = DarkGreen.copy(alpha = 0.3f),
                    timeSelectorUnselectedContainerColor = DarkBg,
                    timeSelectorSelectedContentColor = TextPrimary,
                    timeSelectorUnselectedContentColor = TextSecondary
                )
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = TextSecondary)
                }
                TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                    Text("Salvar", color = DarkGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String =
    "%02d:%02d".format(hour, minute)
