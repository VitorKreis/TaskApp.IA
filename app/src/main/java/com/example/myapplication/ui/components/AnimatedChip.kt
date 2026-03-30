package com.example.myapplication.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.*

@Composable
fun AnimatedChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) DarkGreen.copy(alpha = 0.25f) else SurfaceDark,
        animationSpec = tween(250),
        label = "chip_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) DarkGreen else Purple.copy(alpha = 0.3f),
        animationSpec = tween(250),
        label = "chip_border"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) DarkGreen else TextSecondary,
        animationSpec = tween(250),
        label = "chip_text"
    )

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, color = labelColor) },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = containerColor,
            selectedContainerColor = containerColor,
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = borderColor,
            selectedBorderColor = borderColor,
            enabled = true,
            selected = selected
        ),
        modifier = modifier
    )
}
