package org.example.openaitts.feature.conversation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.example.openaitts.feature.conversation.domain.models.Voice

@Composable
fun VoiceSelectorDialog(
    selectedVoice: Voice,
    onVoiceSelect: (Voice) -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = { onVoiceSelect(selectedVoice) }) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = modifier,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select voice: ",
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(Modifier.height(8.dp))

                enumValues<Voice>().forEach { voice ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onVoiceSelect(voice) }
                    ) {
                        RadioButton(
                            selected = (voice == selectedVoice),
                            onClick = { onVoiceSelect(voice) }
                        )
                        Text(
                            text = voice.name,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}