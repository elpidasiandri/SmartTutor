package org.example.project.tutor.learningList

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.statement.readBytes
import org.jetbrains.skia.Image as SkiaImage
private val httpClient = HttpClient()

@Composable
actual fun RemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier
) {
    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(url) {
        runCatching {
            val bytes = httpClient.get(url).readBytes()
            bitmap = SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
        }
    }

    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = contentDescription,
            modifier = modifier
        )
    }
}
