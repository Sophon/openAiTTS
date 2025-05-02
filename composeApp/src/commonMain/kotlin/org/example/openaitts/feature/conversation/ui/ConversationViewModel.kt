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
import org.example.openaitts.feature.conversation.domain.models.MessageItem
import org.example.openaitts.feature.conversation.domain.models.Role
import org.example.openaitts.feature.conversation.domain.models.Voice
import org.example.openaitts.feature.conversation.domain.usecases.AudioPlaybackUseCase
import org.example.openaitts.feature.conversation.domain.usecases.ConversationUseCase
import org.example.openaitts.feature.conversation.domain.usecases.RecordAudioUseCase
import org.example.openaitts.feature.conversation.domain.usecases.SendConversationMessageUseCase
import org.example.openaitts.feature.conversation.domain.usecases.StopAudioRecordingUseCase
import org.example.openaitts.feature.conversation.domain.usecases.UpdateVoiceUseCase

class ConversationViewModel(
    private val conversationUseCase: ConversationUseCase,
    private val sendMessageUseCase: SendConversationMessageUseCase,
    private val audioPlaybackUseCase: AudioPlaybackUseCase,
    private val updateVoiceUseCase: UpdateVoiceUseCase,
    private val recordAudioUseCase: RecordAudioUseCase,
    private val stopAudioRecordingUseCase: StopAudioRecordingUseCase,
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
//        audioPlaybackUseCase.stop()

        viewModelScope.launch {
            when (
                val result = sendMessageUseCase.sendTextMessage(message = _typedQuery.value, useAudio = true)
            ) {
                is Result.Success -> {
                    val newMessage = UiMessage(
                        type = MessageItem.Type.MESSAGE,
                        role = Role.USER,
                        text = _typedQuery.value,
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

    fun toggleVoiceSelectorDialogVisibility() {
        audioPlaybackUseCase.stop()

        _state.update { it.copy(isVoiceSelectorDialogVisible = it.isVoiceSelectorDialogVisible.not()) }
        Napier.d(tag = TAG) { "selectVoice dialog" }
    }

    fun selectVoice(selected: Voice) {
        toggleVoiceSelectorDialogVisibility()
        _state.update { it.copy(selectedVoice = selected) }

        viewModelScope.launch {
            audioPlaybackUseCase.stop()
            updateVoiceUseCase.updateVoice(selected)
        }
    }

    fun startRecording() {
        _state.update { it.copy(recordingStatus = ConversationViewState.RecordingStatus.RECORDING) }
        recordAudioUseCase.execute()
    }

    fun stopRecording() {
//        _state.update { it.copy(recordingStatus = ConversationViewState.RecordingStatus.DISABLED) }
        _state.update { it.copy(recordingStatus = ConversationViewState.RecordingStatus.IDLE) }
        stopAudioRecordingUseCase.execute()

        viewModelScope.launch {
            when (val result = sendMessageUseCase.sendVoiceMessage()) {
                is Result.Success -> {
                    Napier.d(tag = TAG) { "audio success" }
                }
                is Result.Error -> {
                    Napier.e(tag = TAG) { result.error.toString() }
                    _state.update { it.copy(error = result.error.toString()) }
                }
            }
        }
    }

    private suspend fun connect() {
        conversationUseCase
            .establishConnection()
            .collectLatest { result ->
            when (result) {
                is Result.Success -> {
                    handleMessage(result)
                }
                is Result.Error -> {
                    Napier.e(tag = TAG) { result.error.toString() }
                    _state.update { it.copy(error = result.error.toString(), isLoading = false) }
                }
            }
        }
    }

    private fun handleMessage(result: Result.Success<MessageItem>) {
        _state.update {
            it.copy(messages = it.messages + result.data.toUi(), isLoading = false)
        }
        Napier.d(tag = TAG) { "items: ${_state.value.messages.size}" }
    }
}

private const val TAG = "ConversationViewModel"