package org.example.project.components.loading

import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing4
import org.example.project.dimens.Dimens.spacing48
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun LoadingScreen(
    message: String? = null,
    backgroundColor: Color = Color(0x80000000)
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = spacing4,
                modifier = Modifier.size(spacing48)
            )
            if (!message.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(spacing16))
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}
