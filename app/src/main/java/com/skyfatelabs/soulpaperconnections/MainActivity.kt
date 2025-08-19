package com.skyfatelabs.soulpaperconnections

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skyfatelabs.soulpaperconnections.ui.navigation.AppDestination
import com.skyfatelabs.soulpaperconnections.ui.navigation.AppNavHost
import com.skyfatelabs.soulpaperconnections.ui.theme.SoulPaperTheme
import com.skyfatelabs.soulpaperconnections.viewmodel.AppointmentsViewModel
import com.skyfatelabs.soulpaperconnections.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {

    // Android 13+ notifications permission
    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val initialDest = when (intent?.getStringExtra("open_dest")) {
            "appointments" -> AppDestination.Appointments
            else -> AppDestination.Chat
        }

        setContent {
            SoulPaperTheme {
                // VMs
                val chatVm: ChatViewModel = viewModel()
                val apptVm: AppointmentsViewModel = viewModel(factory = AppointmentsViewModel.factory())

                var current by remember { mutableStateOf(initialDest) }

                // Unread badge for Chat
                val unread by chatVm.unreadCount.collectAsState(initial = 0)

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = current == AppDestination.Chat,
                                onClick = {
                                    current = AppDestination.Chat
                                    chatVm.markAllRead()
                                },
                                icon = {
                                    BadgedBox(
                                        badge = { if (unread > 0) Badge { Text(unread.toString()) } }
                                    ) { Icon(Icons.Outlined.Chat, contentDescription = "Chat") }
                                },
                                label = { Text("Chat") }
                            )
                            NavigationBarItem(
                                selected = current == AppDestination.Appointments,
                                onClick = { current = AppDestination.Appointments },
                                icon = { Icon(Icons.Outlined.Event, contentDescription = "Appointments") },
                                label = { Text("Appointments") }
                            )
                            NavigationBarItem(
                                selected = current == AppDestination.Tickets,
                                onClick = { current = AppDestination.Tickets },
                                icon = { Icon(Icons.Outlined.ShoppingCart, contentDescription = "Tickets") },
                                label = { Text("Tickets") }
                            )
                        }
                    }
                ) { inner ->
                    AppNavHost(
                        modifier = Modifier
                            .padding(inner)
                            .consumeWindowInsets(inner),
                        startDestination = current,
                        chatVm = chatVm,
                        apptVm = apptVm,
                        onNavigate = { dest -> current = dest }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
