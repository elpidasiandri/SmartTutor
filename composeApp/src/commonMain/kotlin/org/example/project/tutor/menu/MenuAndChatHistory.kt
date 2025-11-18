package org.example.project.tutor.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.utils.UtilsComposable.noRippleClickable

@Composable
fun MenuAndChatHistory(
    chatMessages: List<Pair<String, String>>,
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentWidth()
            .background(Color.White)
            .padding(8.dp)
    ) {
        Text(
            "Chat History",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 24.dp, start = 8.dp, end = 8.dp, top = 12.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            chatMessages.forEachIndexed { index, (sender, message) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8E8E8), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "${index + 1}. $message",
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable { onSettingsClick() }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Settings", modifier = Modifier.padding(end = 8.dp), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }

        Column(modifier = Modifier.fillMaxWidth()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .noRippleClickable { },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "My Progress",
                    modifier = Modifier.padding(start = 8.dp,end = 2.dp),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
                Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = "Progress")

                VerticalDivider(modifier = Modifier.padding(start = 8.dp).size(24.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onLogoutClick() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}
