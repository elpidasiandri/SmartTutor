package org.example.project.tutor.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.colors.AppColors.GrayUltraDark
import org.example.project.colors.AppColors.GrayUltraLight
import org.example.project.dimens.Dimens.spacing12
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing24
import org.example.project.dimens.Dimens.spacing4
import org.example.project.dimens.Dimens.spacing8
import org.example.project.strings.SmartTutorStrings.chat_history_title
import org.example.project.strings.SmartTutorStrings.delete_button
import org.example.project.strings.SmartTutorStrings.excersize_title
import org.example.project.strings.SmartTutorStrings.exercises_tab
import org.example.project.strings.SmartTutorStrings.learning_tab
import org.example.project.strings.SmartTutorStrings.my_progress_title
import org.example.project.strings.SmartTutorStrings.new_chat
import org.example.project.strings.SmartTutorStrings.settings_title
import org.example.project.strings.SmartTutorStrings.unit_title
import org.example.project.tutor.models.domainModels.chatHistory.ChatHistoryDomain
import org.example.project.tutor.models.domainModels.chatHistory.SubjectHistoryDomain
import org.example.project.tutor.models.domainModels.learningUnits.ExerciseDomain
import org.example.project.tutor.models.domainModels.tutorChat.TutorTab
import org.example.project.utils.UtilsComposable.noRippleClickable

@Composable
fun MenuAndChatHistory(
    chatHistory: ChatHistoryDomain,
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onLearningUnitClick: (String, String) -> Unit,
    onChatTitleClick: () -> Unit,
    onDeleteSubject: (String) -> Unit,
    onExistingChatClick: (String) -> Unit,
    navigateToMyProgress: () -> Unit,
    onLearningClick: (String, String) -> Unit,
    onExercisesClick: (String, String, Int) -> Unit,
    answeredExerciseKeys: Set<String>,
    currentIndex: Int,
    selectedTab: TutorTab,
    selectedExerciseIndex: Int,
    activeChatId: String,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White)
            .padding(spacing8)
    ) {
        NewChatButton(onChatTitleClick = onChatTitleClick)

        ChatHistoryHeader()

        ChatHistoryList(
            chatHistory = chatHistory,
            onLearningUnitClick = onLearningUnitClick,
            onDeleteSubject = onDeleteSubject,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            onExistingChatClick = { chatId ->
                onExistingChatClick(chatId)
            },
            onLearningClick = { title, subjectId ->
                onLearningClick(title, subjectId)
            },
            onExercisesClick = { title, subjectId, index ->
                onExercisesClick(title, subjectId, index)
            },
            answeredExerciseKeys = answeredExerciseKeys,
            currentIndex = currentIndex,
            selectedTab = selectedTab,
            selectedExerciseIndex = selectedExerciseIndex,
            activeChatId = activeChatId,
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
            onChatTitleClick()
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
    onLearningUnitClick: (String, String) -> Unit,
    onDeleteSubject: (String) -> Unit,
    modifier: Modifier,
    onExistingChatClick: (String) -> Unit,
    onLearningClick: (String, String) -> Unit,
    onExercisesClick: (String, String, Int) -> Unit,
    answeredExerciseKeys: Set<String>,
    currentIndex: Int,
    selectedTab: TutorTab,
    selectedExerciseIndex: Int,
    activeChatId: String,
) {
    Column(modifier = modifier) {
        chatHistory.subjects.forEach { subject ->
            SubjectHistoryItem(
                subject = subject,
                onLearningUnitClick = { title, subjectId ->
                    onLearningUnitClick(title, subjectId)
                },
                onDeleteSubject = { subjectId ->
                    onDeleteSubject(subjectId)
                },
                onExistingChatClick = { chatId ->
                    onExistingChatClick(chatId)
                },
                onLearningClick = { title, subjectId ->
                    onLearningClick(title, subjectId)
                },
                onExercisesClick = { title, subjectId, index ->
                    onExercisesClick(title, subjectId, index)
                },
                answeredExerciseKeys = answeredExerciseKeys,
                currentIndex = currentIndex,
                selectedTab = selectedTab,
                selectedExerciseIndex = selectedExerciseIndex,
                activeChatId = activeChatId,
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
            settings_title,
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
                my_progress_title,
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
    onLearningClick: (String, String) -> Unit,
    onExercisesClick: (String, String, Int) -> Unit,
    answeredExerciseKeys: Set<String>,
    currentIndex: Int,
    selectedTab: TutorTab,
    selectedExerciseIndex: Int,
    activeChatId: String,
) {
    var expanded by remember { mutableStateOf(true) }
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
            subject.learningUnits.forEachIndexed { index, lu ->
                LearningUnitRow(
                    title = "${unit_title} ${index + 1}: ${lu.title}",
                    isRead = lu.isRead,
                    unitIndex = index,
                    exercises = lu.exercises,
                    answeredExerciseKeys = answeredExerciseKeys,
                    onLearningClick = {
                        onLearningClick(lu.title, subject.subjectId)
                    },
                    onExerciseClick = { exerciseIndex ->
                        onExercisesClick(lu.title, subject.subjectId, exerciseIndex)
                    },
                    isCurrentUnit  = index == currentIndex,
                    selectedTab = selectedTab,
                    selectedExerciseIndex = selectedExerciseIndex,
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
            text = { Text(delete_button) },
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
    unitIndex: Int,
    exercises: List<ExerciseDomain>,
    answeredExerciseKeys: Set<String>,
    onLearningClick: () -> Unit,
    onExerciseClick: (Int) -> Unit,
    isCurrentUnit: Boolean,
    selectedTab: TutorTab,
    selectedExerciseIndex: Int,
) {
    var expanded by remember { mutableStateOf(true) }
    var exercisesExpanded by remember(
        isCurrentUnit,
        selectedTab
    ) {
        mutableStateOf(
            isCurrentUnit &&
                    selectedTab == TutorTab.Exercises
        )
    }
    val learningCompleted = isRead

    val exercisesCompleted = exercises.isNotEmpty() &&
            exercises.indices.all { exerciseIndex ->
                answeredExerciseKeys.contains("$unitIndex-$exerciseIndex")
            }

    val unitCompleted = learningCompleted && exercisesCompleted
    val isLearningSelected =
        isCurrentUnit &&
                selectedTab == TutorTab.Learning

    val isExercisesSelected =
        isCurrentUnit &&
                selectedTab == TutorTab.Exercises

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = spacing24, top = spacing8, end = spacing12)
            .background(GrayUltraLight, RoundedCornerShape(spacing12))
            .padding(spacing12)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (unitCompleted) Color(0xFFEAF6EE) else GrayUltraLight,
                    RoundedCornerShape(spacing12)
                )
                .noRippleClickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )

            if (unitCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "unit completed",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier

                        .padding(end = 6.dp)
                        .size(18.dp)
                )
            }

            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                else Icons.Default.KeyboardArrowDown,
                contentDescription = "expand unit"
            )
        }

        if (expanded) {
            Spacer(Modifier.height(spacing8))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        when {
                            isLearningSelected -> Color(0xFFEDE7F6)
                            learningCompleted -> Color(0xFFEAF6EE)
                            else -> Color.Transparent
                        },
                        RoundedCornerShape(spacing8)
                    )
                    .clickable { onLearningClick() }
                    .padding(vertical = spacing8, horizontal = spacing8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = learning_tab,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                if (learningCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "learning completed",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { exercisesExpanded = !exercisesExpanded }
                    .background(
                        when {
                            isExercisesSelected -> Color(0xFFEDE7F6)
                            exercisesCompleted -> Color(0xFFEAF6EE)
                            else -> Color.Transparent
                        },
                        RoundedCornerShape(spacing8)
                    )
                    .padding(vertical = spacing8, horizontal = spacing8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercises_tab,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                if (exercisesCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "exercises completed",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(18.dp)
                    )
                }

                Icon(
                    imageVector = if (exercisesExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = "expand exercises"
                )
            }

            if (exercisesExpanded) {
                exercises.forEachIndexed { exerciseIndex, _ ->
                    val exerciseCompleted =
                        answeredExerciseKeys.contains("$unitIndex-$exerciseIndex")
                    val isExerciseSelected =
                                selectedTab == TutorTab.Exercises &&
                                selectedExerciseIndex == exerciseIndex
                    val exerciseUnlocked =
                        exerciseIndex == 0 ||
                                answeredExerciseKeys.contains("$unitIndex-${exerciseIndex - 1}")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = when {
                                    isExerciseSelected -> Color(0xFFD1C4E9)
                                    !exerciseUnlocked  -> Color(0xFFE0E0E0)
                                    else -> Color.Transparent
                                },
                                shape = RoundedCornerShape(spacing8)
                            )
                            .clickable(enabled = exerciseUnlocked ) { onExerciseClick(exerciseIndex) }
                            .padding(
                                start = spacing24,
                                top = spacing8,
                                bottom = spacing8
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$excersize_title ${exerciseIndex + 1}",
                            fontSize = 12.sp,
                            fontWeight = if (isExerciseSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )

                        if (exerciseCompleted) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "exercise completed",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

