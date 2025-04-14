package org.example.openaitts.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.chat.domain.Message
import org.example.openaitts.feature.chat.domain.SendMessageUseCase

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
): ViewModel() {
    private val _state = MutableStateFlow(ChatViewState())
    val state = _state
        .onStart {}
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChatViewState(),
        )

    fun onChangeQuestion(message: String) {
        _state.update { it.copy(question = message) }
    }

    fun sendMessage() {
        val messageObject = Message(role = "user", content = _state.value.question ?: "")

        viewModelScope.launch {
            when (val response = sendMessageUseCase.execute(messageObject)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            response = response.data.content,
                            error = null,
                        )
                    }
                }

                is Result.Error -> {
                    _state.value = _state.value.copy(error = response.error.toString())
                }
            }
        }
    }
}