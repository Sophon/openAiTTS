package org.example.openaitts.feature.conversation.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.microphone.RECORD_AUDIO
import kotlinx.coroutines.launch
import openaitts.composeapp.generated.resources.Res
import openaitts.composeapp.generated.resources.ic_mic_on
import org.example.openaitts.feature.conversation.ui.ConversationViewState
import org.example.openaitts.theme.localAppColorPalette
import org.jetbrains.compose.resources.painterResource

@Composable
fun RecordButton(
    recordingStatus: ConversationViewState.RecordingStatus,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color: Color
    val action: () -> Unit
    if (recordingStatus == ConversationViewState.RecordingStatus.RECORDING) {
        color = localAppColorPalette.current.lossRed
        action = onStopRecording
    } else {
        color = localAppColorPalette.current.profitGreen
        action = onStartRecording
    }

    //permission stuff
    val coroutineScope = rememberCoroutineScope()
    val permissionFactory = rememberPermissionsControllerFactory()
    val permissionController = remember(permissionFactory) {
        permissionFactory.createPermissionsController()
    }

    BindEffect(permissionController)

    Button(
        onClick = {
            coroutineScope.launch {
                if (permissionController.isPermissionGranted(Permission.RECORD_AUDIO)) {
                    action()
                } else {
                    permissionController.providePermission(Permission.RECORD_AUDIO)
                }
            }
        },
        enabled = (recordingStatus != ConversationViewState.RecordingStatus.DISABLED),
        colors = ButtonDefaults.buttonColors().copy(containerColor = color),
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Icon(painterResource(Res.drawable.ic_mic_on), contentDescription = null)
    }
}