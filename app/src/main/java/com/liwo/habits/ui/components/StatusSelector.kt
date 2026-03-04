package com.liwo.habits.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.liwo.habits.data.model.HabitStatus

@Composable
fun StatusSelector(
    status: HabitStatus,
    onChange: (HabitStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val green = MaterialTheme.colorScheme.primary
    val red = MaterialTheme.colorScheme.error

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        SingleChoiceSegmentedButtonRow {

            // Done
            SegmentedButton(
                selected = status == HabitStatus.DONE,
                onClick = { onChange(HabitStatus.DONE) },
                shape = SegmentedButtonDefaults.itemShape(0, 3),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = green,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                icon = { Icon(Icons.Filled.Check, contentDescription = "Done") },
                label = { Text("Done") }
            )

            // None
            SegmentedButton(
                selected = status == HabitStatus.NONE,
                onClick = { onChange(HabitStatus.NONE) },
                shape = SegmentedButtonDefaults.itemShape(1, 3),
                icon = {}, // IMPORTANT: keeps it from showing any icon slot
                label = { Text("None") }
            )

            // Missed (X)
            SegmentedButton(
                selected = status == HabitStatus.MISSED,
                onClick = { onChange(HabitStatus.MISSED) },
                shape = SegmentedButtonDefaults.itemShape(2, 3),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = red,
                    activeContentColor = MaterialTheme.colorScheme.onError
                ),
                icon = { Icon(Icons.Filled.Close, contentDescription = "Missed") },
                label = { Text("Missed") }
            )
        }
    }
}