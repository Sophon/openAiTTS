package org.example.openaitts.feature.conversation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConversationScreen(
    modifier: Modifier = Modifier,
) {
    val vm = koinViewModel<ConversationViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
}