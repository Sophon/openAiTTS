package org.example.openaitts.feature.conversation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.conversation.domain.ConversationUseCase
import org.example.openaitts.feature.conversation.domain.SendConversationMessageUseCase
import org.example.openaitts.feature.conversation.domain.models.Content
import org.example.openaitts.feature.conversation.domain.models.MessageItem
import org.example.openaitts.feature.conversation.domain.models.Role

class ConversationViewModel(
    private val conversationUseCase: ConversationUseCase,
    private val sendMessageUseCase: SendConversationMessageUseCase,
): ViewModel() {
    private val _state = MutableStateFlow(ConversationViewState())
    val state = _state
        //TODO: why does this not work?
//        .onStart {
//            connect()
//        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ConversationViewState(),
        )

    init {
        viewModelScope.launch {
            connect()
        }
    }

    fun sendMessage(message: String) {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            when (val result = sendMessageUseCase.sendTextMessage(message)) {
                is Result.Success -> {
                    val newMessage = UiMessage(
                        type = MessageItem.Type.MESSAGE,
                        role = Role.USER,
                        content = Content(
                            type = Content.Type.INPUT_TEXT,
                            text = message,
                        )
                    )
                    _state.update { it.copy(messages = it.messages + newMessage) }
                    Napier.d(tag = TAG) { "items: ${_state.value.messages.size}" }
                }
                is Result.Error -> {
                    Napier.e(tag = TAG) { result.error.toString() }
                    _state.update { it.copy(error = result.error.toString()) }
                }
            }
        }
    }

    private suspend fun connect() {
        conversationUseCase.establishConnection().collectLatest { result ->
            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(messages = it.messages + result.data.toUi(), isLoading = false)
                    }
                }
                is Result.Error -> {
                    Napier.e(tag = TAG) { result.error.toString() }
                    _state.update { it.copy(error = result.error.toString(), isLoading = false) }
                }
            }
        }
    }
}

private const val TAG = "ConversationViewModel"