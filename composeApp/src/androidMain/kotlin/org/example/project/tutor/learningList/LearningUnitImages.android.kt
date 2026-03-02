package org.example.project.tutor.learningList

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import coil3.compose.AsyncImage

@Composable
actual fun RemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier
) {
    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        modifier = modifier
    )
}