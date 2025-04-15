package org.example.openaitts.feature.tts.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.openaitts.core.ui.ChatField
import org.example.openaitts.theme.localAppColorPalette
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TtsScreen(
    modifier: Modifier = Modifier,
) {
    val vm = koinViewModel<TtsViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        PlayField(
            playState = state.playState,
            isResponseAvailable = state.isResponseAvailable,
            onPlayClick = vm::playResponse,
            onStopClick = vm::stopPlayingResponse,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.75f)
        )

        ChatField(
            question = state.question,
            onChangeQuestion = vm::onChangeQuestion,
            onSendClick = vm::onSendMessage,
            isEnabled = state.isSendButtonEnabled,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
private fun PlayField(
    playState: TtsViewState.PlayState,
    isResponseAvailable: Boolean,
    onPlayClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(16.dp)
            .background(color = MaterialTheme.colorScheme.background)
            .border(border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground))
    ) {
        val action: () -> Unit
        val icon: ImageVector
        val color: Color

        when (playState) {
            TtsViewState.PlayState.PLAYING -> {
                action = onStopClick
                icon = Icons.Rounded.Close
                color = localAppColorPalette.current.lossRed
            }
            TtsViewState.PlayState.STOPPED -> {
                action = onPlayClick
                icon = Icons.Rounded.PlayArrow
                color = localAppColorPalette.current.profitGreen
            }
        }

        Button(
            onClick = action,
            enabled = isResponseAvailable,
            modifier = Modifier
                .size(128.dp)
                .padding(16.dp)
                .align(Alignment.Center)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
            )
        }
    }
}