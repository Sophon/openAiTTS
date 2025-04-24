package org.example.openaitts.feature.conversation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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
    private val _typedQuery = MutableStateFlow("")
    private val _state = MutableStateFlow(ConversationViewState())
    val state = combine(
        _state,
        _typedQuery
    ) { state, typedQuery ->
        state.copy(query = typedQuery)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ConversationViewState(),
    )


    init {
        viewModelScope.launch {
            connect()
        }
    }

    fun sendMessage() {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            when (
                val result = sendMessageUseCase.sendTextMessage(message = _typedQuery.value, useAudio = true)
            ) {
                is Result.Success -> {
                    val newMessage = UiMessage(
                        type = MessageItem.Type.MESSAGE,
                        role = Role.USER,
                        content = Content(
                            type = Content.Type.INPUT_TEXT,
                            text = _typedQuery.value,
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
            _state.update { it.copy(isLoading = false) }
            onQueryChange()
        }
    }

    fun onQueryChange(query: String = "") {
        _typedQuery.update { query }
    }

    private suspend fun connect() {
        conversationUseCase.establishConnection().collectLatest { result ->
            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(messages = it.messages + result.data.toUi(), isLoading = false)
                    }
                    Napier.d(tag = TAG) { "items: ${_state.value.messages.size}" }
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