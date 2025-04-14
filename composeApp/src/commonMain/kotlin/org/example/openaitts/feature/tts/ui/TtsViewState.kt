package org.example.openaitts.feature.tts.ui

data class TtsViewState(
    val error: String? = null,
    val isLoading: Boolean = false,

    val question: String? = null,
)
