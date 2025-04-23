package org.example.openaitts.feature.conversation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.openaitts.feature.conversation.domain.models.Role
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConversationScreen(
    modifier: Modifier = Modifier,
) {
    val vm = koinViewModel<ConversationViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Button(
            onClick = { vm.sendMessage("who is snow white?") },
            modifier = Modifier
                .align(Alignment.Center)
                .size(128.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
            )
        }

        ChatField(state)

        //TODO: button
    }
}

@Composable
private fun ChatField(state: ConversationViewState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(state.messages) { message ->
            Box(
                contentAlignment = if (message.role == Role.USER)
                    Alignment.CenterEnd
                else
                    Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                ChatBubble(
                    message = message,
                    modifier = Modifier
                        .fillMaxWidth(.75f)
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: UiMessage,
    modifier: Modifier = Modifier,
) {
    Text(
        text = message.content.text,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
    )
}