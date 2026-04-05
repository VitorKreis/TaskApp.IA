package com.example.myapplication.ui.screens.pomodoro

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.presentation.viewmodel.PomodoroPhase
import com.example.myapplication.presentation.viewmodel.PomodoroPreset
import com.example.myapplication.presentation.viewmodel.PomodoroViewModel
import com.example.myapplication.ui.components.DarkTextField
import com.example.myapplication.ui.components.GlassmorphismCard
import com.example.myapplication.ui.components.GradientButton
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val isIdle = state.phase == PomodoroPhase.IDLE
    val isFocus = state.phase == PomodoroPhase.FOCUS
    val isBreak = state.phase == PomodoroPhase.BREAK

    // Pulsing animation when timer is running
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    // Timer ring color
    val ringColor by animateColorAsState(
        targetValue = when {
            isFocus && state.isRunning -> PriorityLow
            isBreak -> PurpleBright
            state.awaitingBreakStart -> PriorityMedium
            else -> GlassBorder
        },
        animationSpec = tween(500),
        label = "ringColor"
    )

    // Format seconds for display
    val minutes = state.remainingSeconds / 60
    val seconds = state.remainingSeconds % 60
    val timeDisplay = "%02d:%02d".format(minutes, seconds)

    // Phase label
    val phaseLabel = when {
        state.awaitingBreakStart -> "Foco concluído! Iniciar pausa?"
        isFocus && state.isRunning -> "Foco"
        isFocus && !state.isRunning -> "Pausado"
        isBreak && state.isRunning -> "Pausa"
        isBreak && !state.isRunning -> "Pausa (pausado)"
        else -> "Pronto para focar"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pomodoro", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.isRunning) viewModel.pause()
                        onNavigateBack()
                    }) {
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Task Name Input ─────────────────────────────────────────
            DarkTextField(
                value = state.taskName,
                onValueChange = { viewModel.setTaskName(it) },
                label = "No que você está trabalhando?",
                singleLine = true
            )

            Spacer(Modifier.height(20.dp))

            // ── Preset Selector ─────────────────────────────────────────
            if (isIdle && !state.awaitingBreakStart) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PomodoroPreset.entries.forEach { preset ->
                        val isSelected = state.preset == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .then(
                                    if (isSelected) Modifier.background(
                                        Brush.horizontalGradient(
                                            listOf(DarkGreen.copy(alpha = 0.3f), Purple.copy(alpha = 0.3f))
                                        )
                                    )
                                    else Modifier.background(SurfaceDark)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) DarkGreen else GlassBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.setPreset(preset) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    preset.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${preset.focusMinutes}min / ${preset.breakMinutes}min",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) DarkGreen else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // ── Timer Display ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .border(
                        width = 6.dp,
                        color = ringColor,
                        shape = CircleShape
                    )
                    .background(SurfaceDark.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = timeDisplay,
                        label = "timer"
                    ) { display ->
                        Text(
                            text = display,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary,
                            modifier = if (state.isRunning) Modifier.alpha(pulseAlpha) else Modifier
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = phaseLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = ringColor
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Cycles Counter ──────────────────────────────────────────
            GlassmorphismCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "🍅",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${state.completedCycles}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Pomodoros",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(48.dp)
                            .background(GlassBorder)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "⏱",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        val totalMin = state.completedCycles * state.preset.focusMinutes
                        val h = totalMin / 60
                        val m = totalMin % 60
                        Text(
                            text = when {
                                h > 0 && m > 0 -> "${h}h ${m}m"
                                h > 0 -> "${h}h"
                                else -> "${m}m"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Total Focado",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(48.dp)
                            .background(GlassBorder)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "📋",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            state.preset.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${state.preset.focusMinutes}/${state.preset.breakMinutes}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Control Buttons ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    // Awaiting break start after focus finished
                    state.awaitingBreakStart -> {
                        // Start break button
                        ActionCircle(
                            icon = Icons.Default.PlayArrow,
                            label = "Pausa",
                            gradient = listOf(Purple, PurpleBright),
                            onClick = { viewModel.startBreak() }
                        )
                        Spacer(Modifier.width(24.dp))
                        // Skip break — go directly to next focus
                        ActionCircle(
                            icon = Icons.Default.Refresh,
                            label = "Pular",
                            gradient = listOf(DarkGreen, DarkGreenDeep),
                            onClick = { viewModel.startFocus() }
                        )
                    }

                    // Idle — ready to start
                    isIdle -> {
                        ActionCircle(
                            icon = Icons.Default.PlayArrow,
                            label = "Iniciar",
                            gradient = listOf(DarkGreen, Purple),
                            size = 80,
                            onClick = { viewModel.startFocus() }
                        )
                    }

                    // Running — can pause
                    state.isRunning -> {
                        ActionCircle(
                            icon = Icons.Default.Pause,
                            label = "Pausar",
                            gradient = listOf(PriorityMedium, PriorityHigh),
                            onClick = { viewModel.pause() }
                        )
                    }

                    // Paused — can resume or reset
                    else -> {
                        ActionCircle(
                            icon = Icons.Default.PlayArrow,
                            label = "Retomar",
                            gradient = listOf(DarkGreen, Purple),
                            onClick = { viewModel.resume() }
                        )
                        Spacer(Modifier.width(24.dp))
                        ActionCircle(
                            icon = Icons.Default.Refresh,
                            label = "Resetar",
                            gradient = listOf(PriorityHigh, PriorityUrgent),
                            onClick = { viewModel.reset() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Finish Session Button ───────────────────────────────────
            if (state.completedCycles > 0 || (isFocus && state.remainingSeconds < state.preset.focusMinutes * 60)) {
                GradientButton(
                    text = "🍅 Finalizar Sessão e Salvar",
                    onClick = {
                        viewModel.finishSession { onNavigateBack() }
                    },
                    gradient = Brush.horizontalGradient(listOf(PriorityHigh, PurpleBright))
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ActionCircle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    gradient: List<Color>,
    size: Int = 64,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(gradient))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TextPrimary,
                modifier = Modifier.size((size * 0.45).dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
