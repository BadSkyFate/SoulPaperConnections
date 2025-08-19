package com.skyfatelabs.soulpaperconnections.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skyfatelabs.soulpaperconnections.viewmodel.SettingsViewModel
import java.time.DayOfWeek

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    vm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory())
) {
    val s by vm.settings.collectAsState()

    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        // Notifications toggle
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Enable notifications", style = MaterialTheme.typography.titleMedium)
                Text("Turn appointment reminders on/off", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = s.notificationsEnabled, onCheckedChange = { vm.setNotificationsEnabled(it) })
        }

        // Default reminder minutes
        ReminderDropdown(
            label = "Default reminder",
            current = s.defaultReminderMinutes,
            options = listOf(0, 5, 10, 15, 30, 60),
            onChange = vm::setDefaultReminder
        )

        // Snooze choices (multi-select)
        SnoozeMultiSelect(
            current = s.snoozeMinutes.toSet(),
            options = listOf(5, 10, 15, 30, 60),
            onChange = { selected ->
                val cleaned = selected.sorted().ifEmpty { listOf(10,15,30) }
                vm.setSnoozeMinutes(cleaned)
            }
        )

        // First day of week
        FirstDayToggle(current = s.firstDayOfWeek, onChange = vm::setFirstDay)

        Spacer(Modifier.height(12.dp))
        Text(
            "Changes are applied immediately. Snooze buttons in notifications will reflect your choices.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderDropdown(label: String, current: Int, options: List<Int>, onChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val labelFor: (Int) -> String = { m -> if (m == 0) "At time" else "$m min before" }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = labelFor(current), onValueChange = {}, readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { m ->
                DropdownMenuItem(text = { Text(labelFor(m)) }, onClick = { onChange(m); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SnoozeMultiSelect(current: Set<Int>, options: List<Int>, onChange: (Set<Int>) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Snooze buttons", style = MaterialTheme.typography.titleMedium)
        Text("Pick the snooze durations shown in notifications", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { m ->
                val selected = current.contains(m)
                FilterChip(
                    selected = selected,
                    onClick = {
                        onChange(if (selected) current - m else current + m)
                    },
                    label = { Text("${m}m") }
                )
            }
        }
    }
}

@Composable
private fun FirstDayToggle(current: DayOfWeek, onChange: (DayOfWeek) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("First day of week", style = MaterialTheme.typography.titleMedium)
        SegmentedButtons(
            items = listOf(DayOfWeek.SUNDAY to "Sun", DayOfWeek.MONDAY to "Mon"),
            current = current,
            onChange = onChange
        )
    }
}

@Composable
private fun <T> SegmentedButtons(items: List<Pair<T, String>>, current: T, onChange: (T) -> Unit) {
    SingleChoiceSegmentedButtonRow {
        items.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = current == value,
                onClick = { onChange(value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = items.size),
                label = { Text(label) }
            )
        }
    }
}
