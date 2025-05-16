package org.example.openaitts.feature.conversation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import openaitts.composeapp.generated.resources.Res
import openaitts.composeapp.generated.resources.ic_mic_off
import openaitts.composeapp.generated.resources.ic_mic_on
import org.jetbrains.compose.resources.painterResource

@Composable
fun ToggleMicButton(
    isMicEnabled: Boolean,
    onToggleMic: () -> Unit,
    isUserTalking: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(15.dp),
        contentAlignment = Alignment.Center
    ) {
        val borderThickness by animateDpAsState(
            if (isUserTalking) {
                24.dp
            } else {
                6.dp
            }
        )

        val background by animateColorAsState(
            if (isMicEnabled.not()) {
                Color.Red
            } else if (isUserTalking) {
                Color.Black
            } else {
                Color.Green.copy(.75f)
            }
        )

        Box(
            Modifier
                .shadow(3.dp, CircleShape)
                .border(borderThickness, Color.White, CircleShape)
                .border(1.dp, Color.Gray, CircleShape)
                .clip(CircleShape)
                .background(background)
                .clickable(onClick = onToggleMic)
                .padding(36.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                painter = painterResource(
                    if (isMicEnabled) {
                        Res.drawable.ic_mic_on
                    } else {
                        Res.drawable.ic_mic_off
                    }
                ),
                tint = Color.White,
                contentDescription = if (isMicEnabled) {
                    "Mute microphone"
                } else {
                    "Unmute microphone"
                },
            )
        }
    }
}