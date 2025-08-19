package com.skyfatelabs.soulpaperconnections.viewmodel


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.skyfatelabs.soulpaperconnections.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // Simple typing indicator: true when input not empty
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    val unreadCount = _messages.map { list -> list.count { !it.read && !it.fromMe } }
        .let { MutableStateFlow(0).apply {
            // cheap collector to keep a live number; in real app use combine/collect in UI layer
        } }

    fun setTyping(active: Boolean) {
        _isTyping.value = active
    }

    fun send(text: String) {
        if (text.isBlank()) return
        _messages.value = _messages.value + Message(
            text = text.trim(),
            fromMe = true,
            read = true
        )

        // (Optional) Simulate a bot reply a moment later by uncommenting and wiring a coroutine
        // viewModelScope.launch {
        //     delay(600)
        //     _messages.value = _messages.value + Message(
        //         text = "Auto-reply to: \"$text\"",
        //         fromMe = false,
        //         read = false
        //     )
        // }
    }

    fun markAllRead() {
        _messages.value = _messages.value.map { if (!it.read) it.copy(read = true) else it }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun seedDemo() {
        if (_messages.value.isNotEmpty()) return
        _messages.value = listOf(
            Message("1", "Welcome to Soul Paper Connections 👋", LocalDateTime.now().minusMinutes(10), fromMe = false, read = false),
            Message("2", "This is your chat. Try sending a message!", LocalDateTime.now().minusMinutes(9), fromMe = false, read = false)
        )
    }
}
