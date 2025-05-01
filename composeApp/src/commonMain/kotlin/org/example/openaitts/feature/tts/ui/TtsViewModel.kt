package org.example.openaitts.feature.tts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.openaitts.core.domain.Result
import org.example.openaitts.feature.audio.AudioFileManager
import org.example.openaitts.feature.tts.domain.PromptTTSUseCase

class TtsViewModel(
    private val promptTTSUseCase: PromptTTSUseCase,
    private val audioFileManager: AudioFileManager,
): ViewModel() {
    private val _state = MutableStateFlow(TtsViewState())
    val state: StateFlow<TtsViewState> = _state

    fun onSendMessage() {
        _state.update { it.copy(isResponseAvailable = false) }

        viewModelScope.launch {
            when (val response = promptTTSUseCase.execute(_state.value.question ?: "")) {
                is Result.Success -> {
//                    audioFileManager.save(response.data)
                    _state.update { it.copy(isResponseAvailable = true) }
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

    fun onChangeQuestion(message: String) {
        _state.update { it.copy(question = message) }
    }

    fun stopPlayingResponse() {
        audioFileManager.stop()
    }

    fun playResponse() {
        audioFileManager.play()
    }
}