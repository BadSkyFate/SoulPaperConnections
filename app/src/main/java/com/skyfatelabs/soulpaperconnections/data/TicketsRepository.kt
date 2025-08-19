package com.skyfatelabs.soulpaperconnections.data

import com.skyfatelabs.soulpaperconnections.model.Ticket

class TicketsRepository(private val dao: TicketDao) {
    val activeTickets = dao.activeTickets()
    val allTickets = dao.allTickets()

    suspend fun upsert(ticket: Ticket) = dao.upsert(ticket)
    suspend fun delete(ticket: Ticket) = dao.delete(ticket)
}
