package com.chase.weather.view

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp

@Composable
fun ButtonWithIcon(
    imageVector: ImageVector,
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(enabled = enabled, onClick = onClick, modifier = modifier) {
        Row {
            Icon(imageVector = imageVector, contentDescription = text)
            Text(text, fontSize = 18.sp)
        }
    }
}