package org.example.project.tutor.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.example.project.colors.AppColors.GrayUltraDark
import org.example.project.colors.AppColors.GrayUltraLight
import org.example.project.dimens.Dimens.spacing12
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing24
import org.example.project.dimens.Dimens.spacing4
import org.example.project.dimens.Dimens.spacing8
import org.example.project.strings.SmartTutorStrings.chat_history_title
import org.example.project.strings.SmartTutorStrings.new_chat
import org.example.project.tutor.models.domainModels.chatHistory.ChatHistoryDomain
import org.example.project.tutor.models.domainModels.chatHistory.SubjectHistoryDomain
import org.example.project.utils.UtilsComposable.noRippleClickable

@Composable
fun MenuAndChatHistory(
    chatHistory: ChatHistoryDomain,
    drawerState: DrawerState,
    scope: CoroutineScope,
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onLearningUnitClick: (String, String) -> Unit,
    onChatTitleClick: () -> Unit,
    onDeleteSubject: (String) -> Unit,
    onExistingChatClick: (String) -> Unit,
    navigateToMyProgress: () -> Unit,

    ) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentWidth()
            .background(Color.White)
            .padding(spacing8)
    ) {
        NewChatButton(onChatTitleClick = onChatTitleClick)

        ChatHistoryHeader()

        ChatHistoryList(
            chatHistory = chatHistory,
            drawerState = drawerState,
            scope = scope,
            onLearningUnitClick = onLearningUnitClick,
            onDeleteSubject = onDeleteSubject,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            onExistingChatClick = { chatId ->
                onExistingChatClick(chatId)
            },
        )

        Spacer(modifier = Modifier.height(spacing12))

        SettingsRow(onSettingsClick = onSettingsClick)

        BottomRow(
            onLogoutClick = onLogoutClick,
            navigateToMyProgress = {
                navigateToMyProgress()
            })
    }
}

@Composable
private fun NewChatButton(onChatTitleClick: () -> Unit) {
    TextButton(
        onClick = {
//            onChatTitleClick()
        },
        modifier = Modifier.padding(vertical = spacing12, horizontal = spacing8)
    ) {
        Text(
            text = new_chat,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
    }
}

@Composable
private fun ChatHistoryHeader() {
    Text(
        chat_history_title,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.padding(vertical = spacing16, horizontal = spacing8)
    )
}

@Composable
private fun ChatHistoryList(
    chatHistory: ChatHistoryDomain,
    drawerState: DrawerState,
    scope: CoroutineScope,
    onLearningUnitClick: (String, String) -> Unit,
    onDeleteSubject: (String) -> Unit,
    modifier: Modifier,
    onExistingChatClick: (String) -> Unit,

    ) {
    Column(modifier = modifier) {
        chatHistory.subjects.forEach { subject ->
            SubjectHistoryItem(
                subject = subject,
                onLearningUnitClick = { title, subjectId ->
                    onLearningUnitClick(title, subjectId)
                    scope.launch { drawerState.close() }
                },
                onDeleteSubject = { subjectId ->
                    onDeleteSubject(subjectId)
                    scope.launch { drawerState.close() }
                },
                onExistingChatClick = { chatId ->
                    onExistingChatClick(chatId)
                }
            )
            Spacer(modifier = Modifier.height(spacing12))
        }
    }
}

@Composable
private fun SettingsRow(onSettingsClick: () -> Unit) {
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
}

@Composable
private fun BottomRow(
    onLogoutClick: () -> Unit,
    navigateToMyProgress: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing8, vertical = spacing12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.noRippleClickable {
                navigateToMyProgress()
            }
        ) {
            Text(
                "My Progress",
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.width(spacing4))
            Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = "Progress")
        }


        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = { onLogoutClick() },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Logout",
                tint = Color.Red
            )
        }
    }
}

@Composable
fun SubjectHistoryItem(
    subject: SubjectHistoryDomain,
    onLearningUnitClick: (String, String) -> Unit,
    onDeleteSubject: (String) -> Unit,
    onExistingChatClick: (String) -> Unit,

    ) {
    var expanded by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column {
        SubjectHeaderRow(
            subject = subject,
            expanded = expanded,
            onRowClick = { expanded = !expanded },
            onToggleExpand = { expanded = !expanded },
            onMoreClick = { menuOpen = true },
            onExistingChatClick = { chatId ->
                onExistingChatClick(chatId)
            },
        )

        SubjectOverflowMenu(
            menuOpen = menuOpen,
            onDismiss = { menuOpen = false },
            onDeleteClick = {
                menuOpen = false
                showDeleteDialog = true
            }
        )

        DeleteSubjectDialog(
            visible = showDeleteDialog,
            subjectTitle = subject.title,
            onConfirm = {
                showDeleteDialog = false
                onDeleteSubject(subject.subjectId)
            },
            onDismiss = { showDeleteDialog = false }
        )

        if (expanded) {
            subject.learningUnits.forEach { lu ->
                LearningUnitRow(
                    title = lu.title,
                    isRead = lu.isRead,
                    onClick = { onLearningUnitClick(lu.title, subject.subjectId) }
                )
            }
        }
    }
}

@Composable
private fun SubjectHeaderRow(
    subject: SubjectHistoryDomain,
    expanded: Boolean,
    onRowClick: () -> Unit,
    onToggleExpand: () -> Unit,
    onMoreClick: () -> Unit,
    onExistingChatClick: (String) -> Unit,

    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GrayUltraDark, RoundedCornerShape(spacing12))
            .noRippleClickable {
                onRowClick()
                onExistingChatClick(subject.subjectId)
            }
            .padding(spacing12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            subject.title,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

//        MoreButton(onClick = onMoreClick, Modifier.height(16.dp).width(24.dp))

        if (subject.learningUnits.isNotEmpty()) {
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "expand",
                modifier = Modifier.noRippleClickable { onToggleExpand() }
            )
        }
    }
}

@Composable
private fun MoreButton(onClick: () -> Unit, modifier: Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More"
        )
    }
}

@Composable
private fun SubjectOverflowMenu(
    menuOpen: Boolean,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    DropdownMenu(
        expanded = menuOpen,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("Delete") },
            onClick = onDeleteClick
        )
    }
}

@Composable
private fun DeleteSubjectDialog(
    visible: Boolean,
    subjectTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete material?") },
        text = {
            Text("This will delete “$subjectTitle” and all its units and chat history. This cannot be undone.")
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun LearningUnitRow(
    title: String,
    isRead: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = spacing24, top = spacing8)
            .background(GrayUltraLight, RoundedCornerShape(spacing12))
            .noRippleClickable { onClick() }
            .padding(spacing12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (!isRead) {
            Box(
                modifier = Modifier
                    .padding(start = 24.dp)
                    .size(8.dp)
                    .background(Color.Red, CircleShape)
            )

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
