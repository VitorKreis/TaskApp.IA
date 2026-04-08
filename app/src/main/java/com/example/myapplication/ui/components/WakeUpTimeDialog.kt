package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WakeUpTimeDialog(
    currentHour: Int,
    currentMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = currentHour,
        initialMinute = currentMinute,
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
            Text(
                text = "⏰ Horário de Despertar",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Defina quando você acorda para receber\nnotificações matinais inteligentes.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TimePicker(
                state = timePickerState,
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
                GradientButton(
                    text = "Salvar",
                    onClick = {
                        onConfirm(timePickerState.hour, timePickerState.minute)
                    }
                )
            }
        }
    }
}
