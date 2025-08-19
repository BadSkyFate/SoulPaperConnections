package com.skyfatelabs.soulpaperconnections.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.skyfatelabs.soulpaperconnections.model.Appointment
import com.skyfatelabs.soulpaperconnections.model.Recurrence
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog

import androidx.compose.material3.ExposedDropdownMenuBox

//import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.Instant

import java.time.ZoneId
import java.time.YearMonth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.graphics.Color


@Composable
fun EditAppointmentDialog(
    appt: Appointment,
    onDismiss: () -> Unit,
    onSave: (title: String, date: LocalDate, time: LocalTime, minutes: Int, recurrence: Recurrence) -> Unit,
    onDelete: () -> Unit
) {
    // Minimal, compiling stub: keeps current values.
    // Replace with full UI later if you want editable fields.
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit appointment") },
        text = { Text("Press Save to keep current values, or Delete to remove.") },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    appt.title,
                    appt.start.toLocalDate(),
                    appt.start.toLocalTime(),
                    appt.reminderMinutes,
                    appt.recurrence
                )
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    )
}

// If FlowRow complains, add this OptIn:
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateAppointmentDialog(
    initialDate: LocalDate = LocalDate.now(),
    onDismiss: () -> Unit,
    onCreate: (title: String, date: LocalDate, time: LocalTime, minutes: Int, recurrence: Recurrence) -> Unit
) {
    // ----- local state -----
    var title by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(initialDate) }
    var time by rememberSaveable { mutableStateOf(LocalTime.now().withSecond(0).withNano(0)) }
    var minutesBefore by rememberSaveable { mutableStateOf(10) }   // default reminder
    var recurrence by rememberSaveable { mutableStateOf(Recurrence.NONE) }

    // pickers
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // ----- Date picker dialog (Material 3) -----
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("Done") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ----- Time picker dialog (Material 3) -----
// ----- Time picker dialog (Material 3) -----
    if (showTimePicker) {
        val ctx = LocalContext.current
        val is24h = DateFormat.is24HourFormat(ctx)
        val configuration = LocalConfiguration.current
        val isLandscape =
            configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        val timeState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = is24h
        )

        Dialog(
            onDismissRequest = { showTimePicker = false },
            // portrait = platform default width; landscape = allow custom width
            properties = DialogProperties(usePlatformDefaultWidth = !isLandscape)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                modifier = if (isLandscape)
                    Modifier
                        .fillMaxWidth()
                        .widthIn(min = 560.dp, max = 840.dp) // wider only on landscape
                else
                    Modifier // portrait untouched
            ) {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // SINGLE clock in both orientations
                    TimePicker(state = timeState)

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        TextButton(onClick = {
                            time = LocalTime.of(timeState.hour, timeState.minute)
                            showTimePicker = false
                        }) { Text("Done") }

                        if (isLandscape) {
                            // 👇 phantom spacer only in landscape
                            Spacer(Modifier.width(80.dp))
                        }
                    }
                }
            }}}


    // ----- Main dialog -----
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New appointment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { showDatePicker = true }) {
                        Text(date.format(DateTimeFormatter.ofPattern("EEE, MMM d")))
                    }
                    FilledTonalButton(onClick = { showTimePicker = true }) {
                        Text(time.format(DateTimeFormatter.ofPattern("h:mm a")))
                    }
                }

                // Reminder dropdown
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = if (minutesBefore == 0) "At time" else "$minutesBefore min before",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Reminder") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf(0, 5, 10, 15, 30, 60).forEach { m ->
                            DropdownMenuItem(
                                text = { Text(if (m == 0) "At time" else "$m min before") },
                                onClick = { minutesBefore = m; expanded = false }
                            )
                        }
                    }
                }

                // Recurrence chips
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Recurrence.values().forEach { r ->
                        FilterChip(
                            selected = recurrence == r,
                            onClick = { recurrence = r },
                            label = {
                                Text(
                                    when (r) {
                                        Recurrence.NONE -> "Does not repeat"
                                        Recurrence.DAILY -> "Daily"
                                        Recurrence.WEEKLY -> "Weekly"
                                        Recurrence.MONTHLY -> "Monthly"
                                    }
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(title.trim().ifBlank { "Untitled" }, date, time, minutesBefore, recurrence)
                }
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

fun shareAppointment(context: Context, appt: Appointment) {
    val whenStr = appt.start.format(DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a"))
    val body = buildString {
        appendLine(appt.title)
        append(whenStr)
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Appointment: ${appt.title}")
        putExtra(Intent.EXTRA_TEXT, body)
    }
    context.startActivity(Intent.createChooser(intent, "Share appointment"))
}
