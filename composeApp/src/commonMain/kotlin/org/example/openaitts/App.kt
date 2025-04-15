package org.example.openaitts

import androidx.compose.runtime.Composable
import org.example.openaitts.feature.chat.ChatScreen
import org.example.openaitts.feature.conversation.ui.ConversationScreen
import org.example.openaitts.feature.tts.ui.TtsScreen
import org.example.openaitts.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    AppTheme {
//        ChatScreen()
//        TtsScreen()
        ConversationScreen()
    }
}