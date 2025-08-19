package com.skyfatelabs.soulpaperconnections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.skyfatelabs.soulpaperconnections.Graph
import com.skyfatelabs.soulpaperconnections.data.TicketsRepository
import com.skyfatelabs.soulpaperconnections.model.Ticket
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class TicketsViewModel(private val repo: TicketsRepository) : ViewModel() {

    // users see only active tickets; admin screen uses allTickets
    val tickets: StateFlow<List<Ticket>> =
        repo.activeTickets.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allTickets: StateFlow<List<Ticket>> =
        repo.allTickets.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun createTicket(name: String, desc: String, priceCents: Int, active: Boolean = true) {
        viewModelScope.launch {
            repo.upsert(Ticket(id = UUID.randomUUID().toString(), name = name, description = desc, priceCents = priceCents, active = active))
        }
    }

    fun updateTicket(t: Ticket) = viewModelScope.launch { repo.upsert(t) }
    fun deleteTicket(t: Ticket) = viewModelScope.launch { repo.delete(t) }

    companion object {
        fun factory() = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                TicketsViewModel(Graph.ticketsRepo) as T
        }
    }
}
