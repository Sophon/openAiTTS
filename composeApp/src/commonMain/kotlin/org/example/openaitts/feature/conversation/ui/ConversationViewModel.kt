package org.example.openaitts.feature.conversation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.example.openaitts.BuildKonfig.API_KEY
import org.example.openaitts.feature.conversation.domain.models.MessageItem
import org.example.openaitts.feature.conversation.domain.models.Role
import org.example.openaitts.feature.conversation.domain.models.Voice
import org.example.openaitts.feature.realtimeAgent.RealtimeAgent
import org.example.openaitts.feature.realtimeAgent.RealtimeAgentCallbacks

class ConversationViewModel: ViewModel() {
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

    fun toggleVoiceSelectorDialogVisibility() {
        _state.update { it.copy(isVoiceSelectorDialogVisible = it.isVoiceSelectorDialogVisible.not()) }
        Napier.d(tag = TAG) { "selectVoice dialog" }
    }

    fun selectVoice(selected: Voice) {
        toggleVoiceSelectorDialogVisibility()
        _state.update { it.copy(selectedVoice = selected) }
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

    private fun addUserMessage(text: String) {
        val newMessage = UiMessage(
            type = MessageItem.Type.MESSAGE,
            role = Role.USER,
            text = text,
        )
        _state.update { it.copy(messages = it.messages + newMessage) }
    }

    private fun appendTranscriptDelta(messageDelta: String) {
        val currentMessages = _state.value.messages.swap()

        _state.update { currentState ->
            val indexLastIncompleteMessage = currentMessages.indexOfLast { it.role == Role.ASSISTANT && it.isIncomplete }
            val lastIncompleteMessage = if (indexLastIncompleteMessage != -1) currentMessages[indexLastIncompleteMessage] else null

            val updatedMessages = if (lastIncompleteMessage == null) {
                val newMessage = UiMessage(
                    type = MessageItem.Type.MESSAGE,
                    role = Role.ASSISTANT,
                    text = messageDelta,
                    isIncomplete = true
                )

                currentMessages + newMessage
            } else {
                val newMessage = lastIncompleteMessage.copy(text = lastIncompleteMessage.text + messageDelta)

                currentMessages.mapIndexed { index, uiMessage ->
                    if (index == indexLastIncompleteMessage) newMessage else uiMessage
                }
            }

            currentState.copy(messages = updatedMessages)
        }
    }

    private fun completeLastTranscript() {
        val currentMessages = _state.value.messages
        val indexLastIncompleteMessage = currentMessages.indexOfLast { it.role == Role.ASSISTANT && it.isIncomplete }
        val lastIncompleteMessage = if (indexLastIncompleteMessage != -1) currentMessages[indexLastIncompleteMessage] else null

        if (lastIncompleteMessage != null) {
            val newMessage = lastIncompleteMessage.copy(isIncomplete = false)
            val newMessages = currentMessages.mapIndexed { index, uiMessage ->
                if (index == indexLastIncompleteMessage) {
                    newMessage
                }
                else uiMessage
            }

            _state.update { it.copy(messages = newMessages) }
        }
    }

    private fun List<UiMessage>.swap(): List<UiMessage> {
        if (this.size < 3) return this

        val lastButOneIndex = this.lastIndex - 1

        return if (this.last().role == Role.USER && this[lastButOneIndex].role == Role.ASSISTANT && this[lastButOneIndex].isIncomplete) {
            val messageList = this.toMutableList()
            val lastUser = messageList.last()
            val lastAssistant = messageList.lastOrNull { it.role == Role.ASSISTANT }!!

            messageList[messageList.lastIndex - 1] = lastUser
            messageList[messageList.lastIndex] = lastAssistant

            messageList
        } else this
    }

    private fun callbacks(): RealtimeAgentCallbacks {
        return object : RealtimeAgentCallbacks {
            //region UNUSED
            override fun onAgentTalking() {
                Napier.d(tag = AGENT_TAG) { "Agent: talking" }
            }
            override fun onAgentTalkingDone() {
                Napier.d(tag = AGENT_TAG) { "Agent: talking done" }
            }
            //endregion

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

            override fun onAgentTranscriptionDeltaReceived(transcriptDelta: String) {
                Napier.d(tag = AGENT_TAG) { "Agent: transcription = $transcriptDelta" }
                appendTranscriptDelta(messageDelta = transcriptDelta)
            }

            override fun onAgentTranscriptionDone() {
                Napier.d(tag = AGENT_TAG) { "Agent: transcription = DONE" }
                completeLastTranscript()
            }

            override fun onUserTranscriptionReceived(text: String, isFinal: Boolean) {
                Napier.d(tag = AGENT_TAG) { "User: = $text" }
                addUserMessage(text = text)
            }

            override fun onUserTalking() {
                Napier.d(tag = AGENT_TAG) { "User: talking" }
                completeLastTranscript()
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
private const val AGENT_TAG = "RealtimeAgentVM"