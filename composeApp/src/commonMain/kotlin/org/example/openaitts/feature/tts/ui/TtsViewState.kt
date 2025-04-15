package org.example.openaitts.feature.tts.ui

data class TtsViewState(
    val question: String? = null,
    val playState: PlayState = PlayState.STOPPED,
    val isResponseAvailable: Boolean = false,

    val error: String? = null,
    val isLoading: Boolean = false,
) {
    enum class PlayState {
        PLAYING,
        STOPPED,
    }

    val isSendButtonEnabled: Boolean get() = question.isNullOrEmpty().not()
}
