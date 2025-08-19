package com.skyfatelabs.soulpaperconnections.data

import com.skyfatelabs.soulpaperconnections.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepository(private val dao: ChatDao) {

    val messages: Flow<List<Message>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun upsert(vararg msgs: Message) {
        dao.upsert(*msgs.map { it.toEntity() }.toTypedArray())
    }

    suspend fun markAllRead() = dao.markAllRead()

    suspend fun clearAll() = dao.clearAll()
}
