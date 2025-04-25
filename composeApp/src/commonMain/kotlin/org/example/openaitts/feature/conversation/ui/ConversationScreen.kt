package org.example.openaitts.feature.conversation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import openaitts.composeapp.generated.resources.Res
import openaitts.composeapp.generated.resources.ic_mic_on
import org.example.openaitts.feature.conversation.domain.models.Role
import org.jetbrains.compose.resources.painterResource
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
            .imePadding()
    ) {
        Column {
            MessagesField(
                messages = state.messages,
                modifier = Modifier
                    .fillMaxHeight(.85f)
                    .border(1.dp, Color.Red),
            )

            Inputs(
                text = state.query,
                onTextChange = vm::onQueryChange,
                onRecord = {},
                onSend = vm::sendMessage,
                modifier = Modifier.border(1.dp, Color.Yellow),
            )
        }
    }
}

@Composable
private fun MessagesField(
    messages: List<UiMessage>,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(index = messages.lastIndex)
    }

    LazyColumn(
        contentPadding = WindowInsets.systemBars.asPaddingValues(),
        state = listState,
        modifier = modifier.fillMaxWidth()
    ) {
        items(messages) { message ->
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
    if (message.text != null) {
        Text(
            text = message.text,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = if (message.role == Role.USER) TextAlign.End else TextAlign.Start,
            modifier = modifier
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
    }
}

@Composable
private fun Inputs(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onRecord: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, Color.Blue)
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSend() })
        )

        Button(
            onClick = onRecord,
            enabled = false,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_mic_on),
                contentDescription = null
            )
        }
    }
}