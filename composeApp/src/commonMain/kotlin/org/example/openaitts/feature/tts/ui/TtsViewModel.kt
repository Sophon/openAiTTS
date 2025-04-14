package org.example.openaitts.feature.tts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.tts.domain.PromptTTSUseCase

class TtsViewModel(
    private val promptTTSUseCase: PromptTTSUseCase,
): ViewModel() {
    private val _state = MutableStateFlow(TtsViewState())
    val state = _state
        .onStart {}
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TtsViewState(),
        )

    fun onSendMessage() {
        viewModelScope.launch {

            when (val response = promptTTSUseCase.execute(_state.value.question ?: "")) {
                is Result.Success -> {
                    val a: ByteArray = response.data
                }
                is Result.Error -> {
                    response.error.toString().let { error ->
                        _state.update { it.copy(error = error) }
                        Napier.e { error }
                    }
                }
            }
        }
    }
}