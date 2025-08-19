// ChatScreen.kt
package com.skyfatelabs.soulpaperconnections.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.skyfatelabs.soulpaperconnections.model.Message
import com.skyfatelabs.soulpaperconnections.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    vm: ChatViewModel
) {
    val messages by vm.messages.collectAsState(initial = emptyList())
    var input by remember { mutableStateOf(TextFieldValue("")) }
    val timeFmt = remember { DateTimeFormatter.ofPattern("h:mm a") }

    LaunchedEffect(Unit) { vm.markAllRead() }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding() // <-- only here; avoids double IME insets
    ) {
        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
                //.padding(horizontal = 12.dp, vertical = 8.dp),
            reverseLayout = true,
            //verticalArrangement = Arrange.ment.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(
                    message = msg,
                    time = msg.timestamp.format(timeFmt)
                )
            }
        }

        // Smooth scroll to newest on change
        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) listState.animateScrollToItem(0)
        }

        // Input row
        Divider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                //.padding(8.dp), // no imePadding here
            //verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
                placeholder = { Text("Type a message") },
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    val text = input.text.trim()
                    if (text.isNotEmpty()) {
                        vm.send(text)
                        input = TextFieldValue("")
                        scope.launch { listState.animateScrollToItem(0) }
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
private fun ChatBubble(message: Message, time: String) {
    val bg =
        if (message.fromMe) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.secondaryContainer

    val horizontalAlign: Alignment.Horizontal =
        if (message.fromMe) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlign
    ) {
        Surface(
            color = bg,
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(message.text)
                Spacer(Modifier.height(4.dp))
                Text(
                    time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
