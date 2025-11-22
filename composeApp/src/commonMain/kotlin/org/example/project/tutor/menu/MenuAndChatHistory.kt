package org.example.project.tutor.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import org.example.project.colors.AppColors.GrayUltraLight
import org.example.project.dimens.Dimens.spacing12
import org.example.project.dimens.Dimens.spacing24
import org.example.project.dimens.Dimens.spacing4
import org.example.project.dimens.Dimens.spacing8
import org.example.project.strings.SmartTutorStrings
import org.example.project.strings.SmartTutorStrings.chat_history_title
import org.example.project.tutor.domainModels.ChatHistoryItemModel
import org.example.project.utils.UtilsComposable.noRippleClickable

@Composable
fun MenuAndChatHistory(
    chatHistory: List<ChatHistoryItemModel>,
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentWidth()
            .background(Color.White)
            .padding(spacing8)
    ) {
        TextButton(
            onClick = {

            },
            modifier = Modifier.padding(
                start = spacing8,
                end = spacing8,
                top = spacing12
            )
        ) {
            Text(
                text = SmartTutorStrings.new_chat,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            )
        }
        Text(
            chat_history_title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(
                bottom = spacing24,
                start = spacing8,
                end = spacing8,
                top = spacing24
            )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            chatHistory.forEach { item ->
                ExpandableChatHistoryItem(item, onChatClick = { title -> onChatClick() })
                Spacer(modifier = Modifier.height(spacing12))
            }
        }

        Spacer(modifier = Modifier.height(spacing12))

        // Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable { onSettingsClick() }
                .padding(horizontal = spacing8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Settings",
                modifier = Modifier.padding(end = spacing8),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }

        // Progress + logout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing8, vertical = spacing12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Progress", fontWeight = FontWeight.Medium, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(spacing4))
            Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = "Progress")

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = { onLogoutClick() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
fun ExpandableChatHistoryItem(
    item: ChatHistoryItemModel,
    onChatClick: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8E8E8), RoundedCornerShape(spacing12))
                .noRippleClickable { onChatClick(item.title) }
                .padding(spacing12),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                item.title,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (item.subItems.isNotEmpty()) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "expand",
                    modifier = Modifier.noRippleClickable { expanded = !expanded }
                )
            }
        }

        if (expanded) {
            item.subItems.forEach { sub ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = spacing24, top = spacing8)
                        .background(GrayUltraLight, RoundedCornerShape(spacing12))
                        .noRippleClickable { onChatClick(sub) }
                        .padding(spacing12),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        sub, fontSize = 12.sp, maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
