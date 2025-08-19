package com.skyfatelabs.soulpaperconnections.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val priceCents: Int,      // store prices as integer cents
    val active: Boolean = true
)
