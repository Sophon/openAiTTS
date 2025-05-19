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
import org.example.openaitts.BuildKonfig.API_KEY
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
import org.example.openaitts.feature.realtimeAgent.RealtimeAgent
import org.example.openaitts.feature.realtimeAgent.RealtimeAgentCallbacks
import org.example.openaitts.feature.transcription.TranscribeAudioMessageUseCase

class ConversationViewModel(
    private val conversationUseCase: ConversationUseCase,
    private val transcribeAudioMessageUseCase: TranscribeAudioMessageUseCase,
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
    private val agent: RealtimeAgent

    init {
//        viewModelScope.launch {
//            connect()
//        }

        agent = RealtimeAgent(callbacks())
    }

    fun sendTextMessage() {
        _state.update { it.copy(isLoading = true) }
//        audioPlaybackUseCase.stop()

        viewModelScope.launch {
            when (
                val result = sendMessageUseCase.sendTextMessage(message = _typedQuery.value, useAudio = true)
            ) {
                is Result.Success -> {
                    addUserMessage(text = _typedQuery.value)
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
        _state.update { it.copy(recordingStatus = ConversationViewState.RecordingStatus.DISABLED) }
        stopAudioRecordingUseCase.execute()

        viewModelScope.launch {
            when (val transcription = transcribeAudioMessageUseCase.execute()) {
                is Result.Success -> {
                    Napier.d(tag = TAG) { "transcription: ${transcription.data}" }
                    addUserMessage(text = transcription.data)
                    sendAudioMessage()
                }
                is Result.Error -> {
                    Napier.e(tag = TAG) { "transcription error: " + transcription.error.toString() }
                    _state.update { it.copy(error = transcription.error.toString()) }
                }
            }
        }
    }

    fun onConnect() {
        _state.update { it.copy(isLoading = true) }
        agent.start(API_KEY, _state.value.selectedVoice)
    }

    fun onDisconnect() {
        agent.stop()
    }

    fun onToggleMic() {
        val newState = _state.value.agentState.isMicEnabled.not()
        agent.toggleMic(newState)
        _state.update { it.copy(agentState = it.agentState.copy(isMicEnabled = newState)) }
        Napier.d(tag = AGENT_TAG) { "new mic value = $newState; state is ${_state.value.agentState.isMicEnabled}" }
    }

    private suspend fun connect() {
//        conversationUseCase.establishRtcConnection(viewModelScope)

        conversationUseCase
            .establishWebSocketConnection()
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
        val currentMessages = _state.value.messages
        if (result.data.isIncomplete.not()) {
            Napier.d(tag = TAG) { "message completed; items: ${currentMessages.size}" }
            _state.update {
                val lastMessage = currentMessages.last().copy(isIncomplete = false)
                it.copy(messages = currentMessages.dropLast(1) + lastMessage, recordingStatus = ConversationViewState.RecordingStatus.IDLE)
            }

            return
        }

        _state.update { currentState ->
            val responses = currentMessages.filter { it.role == Role.ASSISTANT }
            val lastResponse = responses.lastOrNull()
            val updatedMessages = if (lastResponse?.isIncomplete != true) {
                currentMessages + result.data.toUi()
            } else {
                val messageChunk = result.data.content.firstOrNull()?.text.orEmpty()
                val newLastMessage = lastResponse.copy(text = lastResponse.text + messageChunk)
                currentMessages.dropLast(1) + newLastMessage
            }

            currentState.copy(messages = updatedMessages, isLoading = false, recordingStatus = ConversationViewState.RecordingStatus.DISABLED)
        }
    }

    private fun addUserMessage(text: String) {
        val newMessage = UiMessage(
            type = MessageItem.Type.MESSAGE,
            role = Role.USER,
            text = text,
        )
        _state.update { it.copy(messages = it.messages + newMessage) }
    }

    private suspend fun sendAudioMessage() {
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

    private fun handleRtcMessage(message: String) {
        //
    }

    private fun callbacks(): RealtimeAgentCallbacks {
        return object : RealtimeAgentCallbacks {
            override fun onConnect() {
                Napier.d(tag = AGENT_TAG) { "Agent: connected" }
            }

            override fun onDisconnect() {
                Napier.d(tag = AGENT_TAG) { "Agent: disconnected" }
                _state.update { state ->
                    state.copy(agentState = AgentState())
                }
            }

            override fun onBackendError(message: String) {
                Napier.e(tag = AGENT_TAG) { "Agent: backend error $message" }
                _state.update { it.copy(error = message) }
            }

            override fun onAgentReady() {
                Napier.d(tag = AGENT_TAG) { "Agent: ready" }
                _state.update { state ->
                    state.copy(
                        isLoading = false,
                        agentState = state.agentState.copy(isAgentReady = true)
                    )
                }
            }

            override fun onAgentTranscriptionReceived(transcript: String) {
                Napier.d(tag = AGENT_TAG) { "Agent: transcription = $transcript" }

                val newMessage = UiMessage(
                    type = MessageItem.Type.MESSAGE,
                    role = Role.ASSISTANT,
                    text = transcript,
                )
                _state.update { it.copy(messages = it.messages + newMessage) }
            }

            override fun onUserTranscriptionReceived(text: String, isFinal: Boolean) {
                Napier.d(tag = AGENT_TAG) { "User: = $text" }
                addUserMessage(text = text)
            }

            override fun onAgentTalking() {
                Napier.d(tag = AGENT_TAG) { "Agent: talking" }
                _state.update { state ->
                    state.copy(agentState = state.agentState.copy(isAgentTalking = true))
                }
            }

            override fun onAgentTalkingDone() {
                Napier.d(tag = AGENT_TAG) { "Agent: talking done" }
                _state.update { state ->
                    state.copy(agentState = state.agentState.copy(isAgentTalking = false))
                }
            }

            override fun onUserTalking() {
                Napier.d(tag = AGENT_TAG) { "User: talking" }
                _state.update { state ->
                    state.copy(agentState = state.agentState.copy(isUserTalking = true))
                }
            }

            override fun onUserTalkingDone() {
                Napier.d(tag = AGENT_TAG) { "User: talking done" }
                _state.update { state ->
                    state.copy(agentState = state.agentState.copy(isUserTalking = false))
                }
            }
        }
    }
}

private const val TAG = "ConversationViewModel"
private const val AGENT_TAG = "RealtimeAgent"