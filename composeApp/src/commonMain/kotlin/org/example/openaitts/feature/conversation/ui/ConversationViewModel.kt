package org.example.openaitts.feature.conversation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.conversation.domain.ConversationUseCase

class ConversationViewModel(
    private val conversationUseCase: ConversationUseCase,
): ViewModel() {
    private val _state = MutableStateFlow(ConversationViewState())
    val state = _state
        .onStart {
            initializeSession()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ConversationViewState(),
        )

    private suspend fun initializeSession() {
        when (val response = conversationUseCase.startSession()) {
            is Result.Success -> {
                _state.update { it.copy(session = response.data) }
            }
            is Result.Error -> {
                Napier.e(message = response.error.toString(), tag = TAG)
            }
        }
    }
}

private const val TAG = "ConversationViewModel"