package org.example.project.components.customToast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.zIndex
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun CustomToastComposable(
    isError: Boolean,
    message: String,
    modifier: Modifier = Modifier,
    position: Alignment = Alignment.BottomCenter,
    duration: Long = 5000L,
    initializeMessage: (() -> Unit)? = {},
) {
    val isVisible = remember(message) {
        mutableStateOf(true)
    }

    LaunchedEffect(message) {
        delay(duration)

        if (isVisible.value) {
            initializeMessage?.invoke()
            isVisible.value = false
        }
    }

    if (isVisible.value) {
        Box(
            contentAlignment = position,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10000f)
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isError) {
                            Color(0xFFFFF1F1)
                        } else {
                            Color(0xFFF3EEFA)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {


                Spacer(modifier = Modifier.size(32.dp))

                Text(
                    text = message,
                    color = Color(0xFF333333),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        initializeMessage?.invoke()
                        isVisible.value = false
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }
        }
    }
}

