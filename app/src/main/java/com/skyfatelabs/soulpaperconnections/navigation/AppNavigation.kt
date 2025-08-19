package com.skyfatelabs.soulpaperconnections.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.skyfatelabs.soulpaperconnections.ui.screens.AppointmentsScreen
import com.skyfatelabs.soulpaperconnections.viewmodel.AppointmentsViewModel
import com.skyfatelabs.soulpaperconnections.ui.screens.ChatScreen
import com.skyfatelabs.soulpaperconnections.ui.screens.TicketsScreen
import com.skyfatelabs.soulpaperconnections.viewmodel.ChatViewModel

enum class AppDestination { Chat, Appointments, Tickets }

@RequiresApi(Build.VERSION_CODES.O)
@Suppress("UNUSED_PARAMETER")
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    startDestination: AppDestination,
    chatVm: ChatViewModel,
    apptVm: AppointmentsViewModel,
    onNavigate: (AppDestination) -> Unit,
    // Optional deep-link support: when non-null, AppointmentsScreen opens that ID for editing
    openAppointmentId: String? = null
) {
    when (startDestination) {
        AppDestination.Chat -> ChatScreen(
            modifier = modifier,
            vm = chatVm
        )
        AppDestination.Appointments -> AppointmentsScreen(
            modifier = modifier,
            vm = apptVm,
            openAppointmentId = openAppointmentId
        )
        AppDestination.Tickets -> TicketsScreen(
            modifier = modifier,
            isAdmin = true
        )
    }
}
