package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.ui.theme.*
import java.util.Calendar

@Composable
fun DarkTimePickerDialog(
    initialHour: Int = -1,
    initialMinute: Int = -1,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Compute the fallback time atomically so both hour and minute come from
    // the same Calendar instance (avoids a boundary mismatch if defaults are used).
    val now = remember { Calendar.getInstance() }
    var selectedHour by remember {
        mutableIntStateOf(if (initialHour >= 0) initialHour else now.get(Calendar.HOUR_OF_DAY))
    }
    var selectedMinute by remember {
        mutableIntStateOf(if (initialMinute >= 0) initialMinute else now.get(Calendar.MINUTE))
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceDark)
                .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // ── Header ─────────────────────────────────────────────
                Text(
                    "Selecionar Horário",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "%02d:%02d".format(selectedHour, selectedMinute),
                    style = MaterialTheme.typography.headlineLarge,
                    color = DarkGreen,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(20.dp))

                // ── Scroll Wheels ──────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour wheel
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hora", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        ScrollWheel(
                            items = (0..23).toList(),
                            initialIndex = selectedHour,
                            onValueChanged = { selectedHour = it },
                            modifier = Modifier.width(80.dp)
                        )
                    }

                    // Separator
                    Text(
                        ":",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    // Minute wheel
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Minuto", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        ScrollWheel(
                            items = (0..59).toList(),
                            initialIndex = selectedMinute,
                            onValueChanged = { selectedMinute = it },
                            modifier = Modifier.width(80.dp)
                        )
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
                            .clickable { onTimeSelected(selectedHour, selectedMinute); onDismiss() },
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
private fun ScrollWheel(
    items: List<Int>,
    initialIndex: Int,
    onValueChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 48.dp
    val visibleItems = 5
    // The LazyList starts with (visibleItems/2) top-padding items, then the actual items.
    // To center items[initialIndex] visually, the first visible LazyList position must be
    // `initialIndex` (so that the item at position initialIndex + visibleItems/2 is centered).
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    val centeredIndex by remember {
        derivedStateOf {
            val first = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            val halfItem = 60 // approximate half item height in pixels
            // centeredIndex is an index into `items`. The top-padding takes up (visibleItems/2)
            // LazyList positions, so the items array index = first + (offset > halfItem ? 1 : 0).
            first + if (offset > halfItem) 1 else 0
        }
    }

    LaunchedEffect(centeredIndex) {
        if (centeredIndex in items.indices) {
            onValueChanged(items[centeredIndex])
        }
    }

    Box(
        modifier = modifier
            .height(itemHeight * visibleItems)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkBgAlt)
            .border(1.dp, Purple.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .matchParentSize()
                .drawWithContent {
                    drawContent()
                    // Top fade
                    drawRect(
                        Brush.verticalGradient(
                            listOf(SurfaceDark, Color.Transparent),
                            startY = 0f,
                            endY = size.height * 0.3f
                        )
                    )
                    // Bottom fade
                    drawRect(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, SurfaceDark),
                            startY = size.height * 0.7f,
                            endY = size.height
                        )
                    )
                }
        ) {
            // Padding items to center first/last
            items(visibleItems / 2) {
                Box(Modifier.height(itemHeight))
            }
            items(items.size) { index ->
                val isCentered = index == centeredIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .then(
                            if (isCentered) Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkGreen.copy(alpha = 0.2f))
                            else Modifier
                        )
                        .clickable {
                            onValueChanged(items[index])
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "%02d".format(items[index]),
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = if (isCentered) 24.sp else 18.sp,
                        color = if (isCentered) TextPrimary else TextSecondary.copy(alpha = 0.4f),
                        fontWeight = if (isCentered) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
            // Padding items at bottom
            items(visibleItems / 2) {
                Box(Modifier.height(itemHeight))
            }
        }

        // Center highlight bar
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .padding(horizontal = 6.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, DarkGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
        )
    }
}
