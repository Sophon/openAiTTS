package org.example.openaitts.feature.conversation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.openaitts.core.domain.onError
import org.example.openaitts.core.domain.onSuccess
import org.example.openaitts.feature.chat.domain.Message
import org.example.openaitts.feature.conversation.domain.ConversationUseCase
import org.example.openaitts.feature.conversation.domain.SendConversationMessageUseCase

class ConversationViewModel(
    private val conversationUseCase: ConversationUseCase,
    private val sendMessageUseCase: SendConversationMessageUseCase,
): ViewModel() {
    private val _state = MutableStateFlow(ConversationViewState())
    val state = _state
        .onStart {
            connect()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ConversationViewState(),
        )
    private val _receivedMessageEvents = Channel<Message>(capacity = Channel.BUFFERED)
    val receivedMessageEvents = _receivedMessageEvents.receiveAsFlow()

    fun sendMessage(message: String) {
        viewModelScope.launch {
            sendMessageUseCase.sendMessage(message)
                .onError { Napier.e(tag = TAG) { it.toString() } }
        }
    }

    private suspend fun connect() {
        conversationUseCase.establishConnection()
            .onSuccess { flow ->
                flow.collect { _receivedMessageEvents.send(Message(role = "TODO", content = it)) }

                sendMessage("who is snow white?")
            }
    }
}

private const val TAG = "ConversationViewModel"