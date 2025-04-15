package org.example.openaitts.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.openaitts.theme.localAppColorPalette

@Composable
fun ChatField(
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