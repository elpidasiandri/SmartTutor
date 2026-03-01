package org.example.project.components.loadImages

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import kotlinproject.shared.generated.resources.Res

expect fun decodeImage(bytes: ByteArray): ImageBitmap

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ResourceImage(res: DrawableResource, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(res),
        contentDescription = null,
        modifier = modifier
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun rememberImageBitmapFromResources(path: String?): ImageBitmap? {
    var bmp by remember(path) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(path) {
        if (path.isNullOrBlank()) {
            bmp = null; return@LaunchedEffect
        }
        runCatching {
            val bytes = Res.readBytes(path)
            decodeImage(bytes)
        }.onSuccess { bmp = it }
            .onFailure { bmp = null }
    }

    return bmp
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun rememberBmpFromResPath(resourcePath: String?): ImageBitmap? {
    var bmp by remember(resourcePath) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(resourcePath) {
        if (resourcePath.isNullOrBlank()) {
            bmp = null; return@LaunchedEffect
        }

        runCatching {
            val bytes = Res.readBytes(resourcePath)
            decodeImage(bytes)
        }.onSuccess { bmp = it }
            .onFailure { e ->
                bmp = null
            }
    }
    return bmp
}

fun normalizeImagePath(relativePath: String): String {
    val trimmed = relativePath.removePrefix("/")
    return when {
        trimmed.startsWith("files/") -> trimmed
        trimmed.startsWith("images/") -> "files/$trimmed"
        else -> "files/images/$trimmed"
    }
}

fun imageNumberToResourcePath(num: String): String =
    "files/images/image_${num}.png" // image_2.1.0.png
