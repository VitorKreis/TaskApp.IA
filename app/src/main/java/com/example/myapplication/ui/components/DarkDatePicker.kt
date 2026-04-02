package com.example.myapplication.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth

private val ptMonths = mapOf(
    1 to "Janeiro", 2 to "Fevereiro", 3 to "Março", 4 to "Abril",
    5 to "Maio", 6 to "Junho", 7 to "Julho", 8 to "Agosto",
    9 to "Setembro", 10 to "Outubro", 11 to "Novembro", 12 to "Dezembro"
)

@Composable
fun DarkDatePickerDialog(
    initialDate: LocalDate = LocalDate.now(),
    minDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceDark)
                .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column {
                // ── Header ─────────────────────────────────────────────
                Text(
                    "Selecionar Data",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${selectedDate.dayOfMonth} de ${ptMonths[selectedDate.monthValue]} ${selectedDate.year}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkGreen
                )

                Spacer(Modifier.height(16.dp))

                // ── Month Navigation ───────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Anterior", tint = TextPrimary)
                    }
                    Text(
                        "${ptMonths[currentMonth.monthValue]} ${currentMonth.year}",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Próximo", tint = TextPrimary)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Day Headers ────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("S", "T", "Q", "Q", "S", "S", "D").forEach { d ->
                        Text(
                            text = d,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // ── Calendar Grid ──────────────────────────────────────
                val firstDay = currentMonth.atDay(1)
                val startOffset = firstDay.dayOfWeek.value - 1
                val daysInMonth = currentMonth.lengthOfMonth()
                val rows = (startOffset + daysInMonth + 6) / 7
                val today = LocalDate.now()

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (row in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 0 until 7) {
                                val dayNum = row * 7 + col - startOffset + 1
                                if (dayNum in 1..daysInMonth) {
                                    val date = currentMonth.atDay(dayNum)
                                    val isSelected = date == selectedDate
                                    val isToday = date == today
                                    val isPast = minDate != null && date.isBefore(minDate)

                                    DateCell(
                                        day = dayNum,
                                        isSelected = isSelected,
                                        isToday = isToday,
                                        isPast = isPast,
                                        onClick = { if (!isPast) selectedDate = date },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Buttons ────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceVariantDark)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancelar", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(DarkGreen, Purple)))
                            .clickable { onDateSelected(selectedDate); onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Confirmar", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun DateCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    isPast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgModifier = when {
        isSelected -> Modifier
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(DarkGreen, Purple)))
        isToday -> Modifier
            .clip(CircleShape)
            .border(1.dp, DarkGreen, CircleShape)
        else -> Modifier.clip(CircleShape)
    }

    val textColor = when {
        isSelected -> TextPrimary
        isPast -> TextSecondary.copy(alpha = 0.3f)
        isToday -> DarkGreen
        else -> TextPrimary.copy(alpha = 0.9f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .then(bgModifier)
            .clickable(enabled = !isPast, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}
