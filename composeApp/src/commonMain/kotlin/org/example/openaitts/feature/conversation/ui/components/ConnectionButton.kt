package org.example.openaitts.feature.conversation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun ConnectionButton(
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)

    val clickAction: () -> Unit
    val background: Color
    val text: String
    if (isConnected) {
        text = "Disconnect"
        clickAction = onDisconnect
        background = Color.Red
    } else {
        text = "Connect"
        clickAction = onConnect
        background = Color.Green.copy(.75f)
    }

    Row(
        modifier
            .border(1.dp, MaterialTheme.colorScheme.onPrimary, shape)
            .clip(shape)
            .clickable(onClick = clickAction)
            .background(background)
            .padding(vertical = 10.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            imageVector = Icons.Default.Call,
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}