package org.example.openaitts.feature.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.openaitts.theme.localAppColorPalette
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatScreen(modifier: Modifier = Modifier) {
    val vm = koinViewModel<ChatViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = state.response ?: "",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(.5f)
                .border(border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground))
        )

        ChatField(
            question = state.question,
            onChangeQuestion = vm::onChangeQuestion,
            onSendClick = vm::sendMessage,
            isEnabled = state.isButtonEnabled,
            modifier = Modifier.fillMaxSize(.5f)
        )
    }
}

@Composable
private fun ChatField(
    question: String?,
    onChangeQuestion: (String) -> Unit,
    onSendClick: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(16.dp),
    ) {
        TextField(
            value = question ?: "",
            onValueChange = onChangeQuestion,
        )

        Button(
            onClick = onSendClick,
            colors = ButtonDefaults.buttonColors().copy(containerColor = localAppColorPalette.current.profitGreen),
            enabled = isEnabled
        ) {
            Text(text = "Send")
        }
    }
}