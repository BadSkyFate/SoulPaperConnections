package com.skyfatelabs.soulpaperconnections.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skyfatelabs.soulpaperconnections.model.Ticket
import com.skyfatelabs.soulpaperconnections.viewmodel.TicketsViewModel
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreen(
    modifier: Modifier = Modifier,
    vm: TicketsViewModel = viewModel(factory = TicketsViewModel.factory()),
    isAdmin: Boolean = true // toggle this however you authenticate admins
) {
    var tab by remember { mutableStateOf(0) } // 0=Shop, 1=Manage
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tickets") },
                actions = { Icon(Icons.Outlined.ShoppingCart, contentDescription = null) }
            )
        }
    ) { inner ->
        Column(modifier.padding(inner)) {
            if (isAdmin) {
                TabRow(selectedTabIndex = tab) {
                    Tab(selected = tab==0, onClick={tab=0}, text = { Text("Shop") })
                    Tab(selected = tab==1, onClick={tab=1}, text = { Text("Manage") })
                }
            }
            if (!isAdmin || tab==0) ShopTab(vm = vm, modifier = Modifier.fillMaxSize())
            else AdminTab(vm = vm, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun ShopTab(vm: TicketsViewModel, modifier: Modifier = Modifier) {
    val tickets by vm.tickets.collectAsState()
    var quantities by remember { mutableStateOf(mapOf<String,Int>()) }

    fun price(cents: Int) = "$${"%.2f".format(cents / 100.0)}"
    val totalCents = tickets.sumOf { t -> (quantities[t.id] ?: 0) * t.priceCents }

    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (tickets.isEmpty()) {
            Text("No tickets available.")
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tickets, key = { it.id }) { t ->
                ElevatedCard {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(t.name, style = MaterialTheme.typography.titleMedium)
                        if (t.description.isNotBlank()) Text(t.description, style = MaterialTheme.typography.bodyMedium)
                        Text(price(t.priceCents), style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val q = quantities[t.id] ?: 0
                            OutlinedButton(onClick = { quantities = quantities + (t.id to max(0, q-1)) }) { Text("-") }
                            Text("$q", modifier = Modifier.padding(horizontal = 8.dp))
                            Button(onClick = { quantities = quantities + (t.id to (q+1)) }) { Text("+") }
                        }
                    }
                }
            }
        }

        Divider()
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total", style = MaterialTheme.typography.titleMedium)
            Text(price(totalCents), style = MaterialTheme.typography.titleMedium)
        }
        Button(
            enabled = totalCents > 0,
            onClick = {
                // TODO: call your backend to create PaymentIntent, then present Stripe PaymentSheet
                // For now, stub a success and clear cart:
                // show snackbar or navigate to "Thanks" screen
                quantities = emptyMap()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Pay") }
    }
}

@Composable
private fun AdminTab(vm: TicketsViewModel, modifier: Modifier = Modifier) {
    val all by vm.allTickets.collectAsState()
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var desc by remember { mutableStateOf(TextFieldValue("")) }
    var price by remember { mutableStateOf(TextFieldValue("")) } // dollars
    var active by remember { mutableStateOf(true) }

    fun toCents(text: String): Int =
        ((text.toDoubleOrNull() ?: 0.0) * 100).toInt()

    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Create / Update Ticket", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(desc, { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(price, { price = it }, label = { Text("Price (USD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = active, onCheckedChange = { active = it })
            Text("Active")
        }
        Button(
            onClick = {
                vm.createTicket(
                    name.text.trim().ifBlank { "Untitled" },
                    desc.text.trim(),
                    toCents(price.text),
                    active
                )
                name = TextFieldValue(""); desc = TextFieldValue(""); price = TextFieldValue(""); active = true
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save Ticket") }

        Divider(Modifier.padding(vertical = 8.dp))
        Text("All tickets", style = MaterialTheme.typography.titleMedium)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(all, key = { it.id }) { t ->
                ListItem(
                    headlineContent = { Text(t.name) },
                    supportingContent = { Text(t.description) },
                    trailingContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("$${"%.2f".format(t.priceCents / 100.0)}")
                            AssistChip(onClick = {
                                vm.updateTicket(t.copy(active = !t.active))
                            }, label = { Text(if (t.active) "Deactivate" else "Activate") })
                            AssistChip(onClick = { vm.deleteTicket(t) }, label = { Text("Delete") })
                        }
                    }
                )
                Divider()
            }
        }
    }
}
