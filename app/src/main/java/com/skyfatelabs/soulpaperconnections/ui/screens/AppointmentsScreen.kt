@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.skyfatelabs.soulpaperconnections.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.skyfatelabs.soulpaperconnections.model.Appointment
import com.skyfatelabs.soulpaperconnections.model.Recurrence
import com.soulpaper.connections.ui.components.CalendarGrid
import com.skyfatelabs.soulpaperconnections.viewmodel.AppointmentsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppointmentsScreen(
    modifier: Modifier = Modifier,
    vm: AppointmentsViewModel,
    openAppointmentId: String? = null
) {
    val appointments by vm.appointments.collectAsState()
    var editingAppt by remember { mutableStateOf<Appointment?>(null) }

    // Open Edit dialog when launched from a deep link
    LaunchedEffect(openAppointmentId, appointments) {
        val id = openAppointmentId
        if (id != null) {
            appointments.firstOrNull { it.id == id }?.let { appt ->
                editingAppt = appt
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showCreate by remember { mutableStateOf(false) }

    // Calendar state
    var calendarMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // date -> count for grid shading
    val countsByDate = remember(appointments) {
        appointments.groupingBy { it.start.toLocalDate() }.eachCount()
    }

    fun requestDelete(appt: Appointment) {
        vm.deleteAppointment(appt.id)
        scope.launch {
            val res = snackbarHostState.showSnackbar(
                message = "Deleted \"${appt.title}\"",
                actionLabel = "Undo",
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            if (res == SnackbarResult.ActionPerformed) {
                vm.restoreAppointment(appt)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Appointments") },
                actions = { FilledTonalButton(onClick = { showCreate = true }) { Text("New") } }
            )
        }
    ) { inner ->

        val scroll = rememberScrollState()
        val fmt = remember { DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a") }
        val context = LocalContext.current

        Column(
            modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(scroll)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Calendar grid with shading + month nav
            CalendarGrid(
                month = calendarMonth,
                appointmentsByDate = countsByDate,
                selectedDate = selectedDate,
                onSelectDate = { sel ->
                    selectedDate = sel
                    if (sel != null) calendarMonth = YearMonth.from(sel)
                },
                onChangeMonth = { newMonth ->
                    calendarMonth = newMonth
                    if (selectedDate != null && YearMonth.from(selectedDate!!) != newMonth) {
                        selectedDate = null
                    }
                },
                onLongPressDate = { d ->
                    selectedDate = d
                    calendarMonth = YearMonth.from(d)
                    showCreate = true
                }
            )


            // Selected-day chip
            selectedDate?.let { day ->
                AssistChip(
                    onClick = { selectedDate = null },
                    label = { Text("Showing: " + day.format(DateTimeFormatter.ofPattern("EEE, MMM d"))) }
                )
            }

            // Filtered list
            val list = selectedDate?.let { d ->
                appointments.filter { it.start.toLocalDate() == d }
            } ?: appointments

            if (list.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (selectedDate == null) "No appointments yet. Tap New to create one."
                        else "No appointments on this day."
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    list.forEach { appt ->
                        key(appt.id) {
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    when (value) {
                                        SwipeToDismissBoxValue.StartToEnd -> {
                                            // swipe RIGHT → Edit (do not dismiss)
                                            editingAppt = appt
                                            false
                                        }

                                        SwipeToDismissBoxValue.EndToStart -> {
                                            // swipe LEFT → Delete (allow dismiss)
                                            requestDelete(appt)
                                            true
                                        }

                                        else -> false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = true,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    val value = dismissState.targetValue
                                    val bg = when (value) {
                                        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                    val icon = when (value) {
                                        SwipeToDismissBoxValue.StartToEnd -> Icons.Filled.Edit
                                        SwipeToDismissBoxValue.EndToStart -> Icons.Filled.Delete
                                        else -> null
                                    }
                                    Row(
                                        Modifier
                                            .fillMaxSize()
                                            .background(bg)
                                            .padding(horizontal = 24.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = when (value) {
                                            SwipeToDismissBoxValue.StartToEnd -> Arrangement.Start
                                            SwipeToDismissBoxValue.EndToStart -> Arrangement.End
                                            else -> Arrangement.Center
                                        }
                                    ) { icon?.let { Icon(it, contentDescription = null) } }
                                },
                                content = {
                                    ListItem(
                                        headlineContent = { Text(appt.title) },
                                        supportingContent = {
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(appt.start.format(fmt))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    AssistChip(
                                                        onClick = {},
                                                        label = {
                                                            Text(
                                                                if (appt.reminderMinutes == 0) "At time"
                                                                else "${appt.reminderMinutes} min before"
                                                            )
                                                        }
                                                    )
                                                    if (appt.recurrence != Recurrence.NONE) {
                                                        AssistChip(
                                                            onClick = {},
                                                            label = { Text(recurrenceLabel(appt.recurrence)) }
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        trailingContent = {
                                            var menuExpanded by remember { mutableStateOf(false) }
                                            Box {
                                                IconButton(onClick = { menuExpanded = true }) {
                                                    Icon(
                                                        Icons.Filled.MoreVert,
                                                        contentDescription = "More"
                                                    )
                                                }
                                                DropdownMenu(
                                                    expanded = menuExpanded,
                                                    onDismissRequest = { menuExpanded = false }
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text("Edit") },
                                                        onClick = {
                                                            menuExpanded = false
                                                            editingAppt = appt
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("Delete") },
                                                        onClick = {
                                                            menuExpanded = false
                                                            requestDelete(appt)
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("Share") },
                                                        onClick = {
                                                            menuExpanded = false
                                                            shareAppointment(context, appt)
                                                        }
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = { /* could open details */ },
                                                onLongClick = { editingAppt = appt }
                                            )
                                    )
                                }
                            )
                            Divider()
                        }
                    }
                }
            }

            // keep bottom content from being hidden by system bars
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }

        editingAppt?.let { appt ->
            EditAppointmentDialog(
                appt = appt,
                onDismiss = { editingAppt = null },
                onSave = { title, date, time, minutes, recurrence ->
                    vm.updateAppointment(appt.id, title, date, time, minutes, recurrence)
                    calendarMonth = YearMonth.from(date)
                    selectedDate = date
                    editingAppt = null
                },
                onDelete = {
                    requestDelete(appt)
                    editingAppt = null
                }
            )
        }
    }

// NEW: Creation dialog
    if (showCreate) {
        CreateAppointmentDialog(
            initialDate = selectedDate ?: LocalDate.now(),
            onDismiss = { showCreate = false },
            onCreate = { title, date, time, minutes, recurrence ->
                vm.addAppointment(title, date, time, minutes, recurrence)
                // keep calendar state in sync
                calendarMonth = YearMonth.from(date)
                selectedDate = date
                showCreate = false
            }
        )
    }
}
private fun recurrenceLabel(r: Recurrence) = when (r) {
    Recurrence.NONE -> "Does not repeat"
    Recurrence.DAILY -> "Daily"
    Recurrence.WEEKLY -> "Weekly"
    Recurrence.MONTHLY -> "Monthly"
}
