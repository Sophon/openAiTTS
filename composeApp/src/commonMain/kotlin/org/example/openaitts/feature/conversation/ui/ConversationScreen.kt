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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import org.example.openaitts.feature.conversation.domain.models.Role
import org.example.openaitts.feature.conversation.ui.components.RecordButton
import org.example.openaitts.feature.conversation.ui.components.VoiceSelectorDialog
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConversationScreen(
    modifier: Modifier = Modifier,
) {
    val vm = koinViewModel<ConversationViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopBar(
                onClick = vm::toggleVoiceSelectorDialogVisibility,
                selectedVoiceInitials = state.selectedVoiceInitials,
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
                .padding(paddingValues)
        ) {
            Content(
                vm = vm,
                state = state,
            )

            if (state.isVoiceSelectorDialogVisible) {
                VoiceSelectorDialog(
                    selectedVoice = state.selectedVoice,
                    onVoiceSelect = vm::selectVoice,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onClick: () -> Unit,
    selectedVoiceInitials: String,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Text(
                text = "MagicWare chat",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        actions = {
            IconButton(onClick = onClick) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.onPrimary, shape = CircleShape)
                ) {
                    Text(
                        text = selectedVoiceInitials,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = modifier,
    )
}

@Composable
private fun Content(
    vm: ConversationViewModel,
    state: ConversationViewState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        MessagesField(
            messages = state.messages,
            modifier = Modifier
                .fillMaxHeight(.85f)
                .border(1.dp, Color.Red),
        )

        Inputs(
            text = state.query,
            onTextChange = vm::onQueryChange,
            isSendEnabled = state.isSendEnabled,
            onSend = vm::sendTextMessage,
            recordingStatus = state.recordingStatus,
            onStartRecording = vm::startRecording,
            onStopRecording = vm::stopRecording,
            modifier = Modifier.border(1.dp, Color.Yellow),
        )
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
    isSendEnabled: Boolean,
    onSend: () -> Unit,
    recordingStatus: ConversationViewState.RecordingStatus,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Blue)
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSend() }),
            trailingIcon = {
                Button(
                    onClick = onSend,
                    enabled = isSendEnabled,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(Icons.AutoMirrored.Default.Send, contentDescription = null)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        RecordButton(
            recordingStatus = recordingStatus,
            onStartRecording = onStartRecording,
            onStopRecording = onStopRecording,
        )
    }
}