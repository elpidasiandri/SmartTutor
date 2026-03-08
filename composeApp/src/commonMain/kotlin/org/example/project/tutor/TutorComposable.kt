package org.example.project.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.project.AppConfig
import org.example.project.colors.AppColors
import org.example.project.colors.AppColors.GrayLightBoard
import org.example.project.components.customToast.CustomToastComposable
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing24
import org.example.project.dimens.Dimens.spacing4
import org.example.project.dimens.Dimens.spacing8
import org.example.project.strings.SmartTutorStrings.learning_unit_title
import org.example.project.strings.SmartTutorStrings.material_title
import org.example.project.strings.SmartTutorStrings.type_your_message
import org.example.project.strings.SmartTutorStrings.uploaded_file_failure_title
import org.example.project.strings.SmartTutorStrings.uploaded_file_success_title
import org.example.project.tutor.domainModels.chatHistory.ChatHistoryDomain
import org.example.project.tutor.domainModels.learningUnits.LearningUnitContentDomain
import org.example.project.tutor.domainModels.tutorChat.ChatSessionStartDomain
import org.example.project.tutor.domainModels.tutorChat.ChatUiMessage
import org.example.project.tutor.domainModels.tutorChat.LearningUnitDomain
import org.example.project.tutor.domainModels.tutorChat.MaterialDomain
import org.example.project.tutor.domainModels.tutorChat.SenderType
import org.example.project.tutor.domainModels.tutorChat.TutorTab
import org.example.project.tutor.excercise.ExercisesTab
import org.example.project.tutor.learningList.LearningMessagesList
import org.example.project.tutor.menu.MenuAndChatHistory
import org.example.project.tutor.utils.normalizeForHeaderCompare
import org.koin.compose.koinInject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun TutorScreen(
    chatData: ChatSessionStartDomain,
    chatHistory: ChatHistoryDomain,
    showCustomMessage: Boolean,
    message: String,
    isError: Boolean,
    userMessage: String,
    widthDividerMenu: Int = 2,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onMessageDismiss: () -> Unit,
    onUploadPdfClick: () -> Unit,
    selectLearningUnits: () -> Unit,
    onNextUnitClick: () -> Unit,
    onExercisesClick: () -> Unit,
    startNewChat: () -> Unit,
    deleteSubject: (String) -> Unit,
    isWebFlow: Boolean = false,
    isLoading: Boolean = false,
    onChangeTab: (TutorTab) -> Unit,
    selectedTab: TutorTab,
    tabInfoMessage: String,
    currentUnit: LearningUnitContentDomain,
) {

    Box(modifier = Modifier.fillMaxSize()) {
        TutorChatUI(
            chatData = chatData,
            chatHistory = chatHistory,
            showCustomMessage = showCustomMessage,
            message = message,
            isError = isError,
            userMessage = userMessage,
            widthDividerMenu = widthDividerMenu,
            onSettingsClick = { onSettingsClick() },
            onLogoutClick = { onLogoutClick() },
            onMessageDismiss = {
                onMessageDismiss()
            },
            onUploadPdfClick = {
                onUploadPdfClick()
            },
            selectLearningUnits = {
                selectLearningUnits()
            },
            onNextUnitClick = {
                onNextUnitClick()
            },
            onExercisesClick = {
                onExercisesClick()
            },
            startNewChat = {
                startNewChat()
            },
            deleteSubject = { p1 ->
                deleteSubject(p1)
            },
            selectedTab = selectedTab,
            onChangeTab = { onChangeTab(it) },
            tabInfoMessage = tabInfoMessage,
            currentUnit = currentUnit
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun TutorChatUI(
    chatData: ChatSessionStartDomain,
    chatHistory: ChatHistoryDomain,
    showCustomMessage: Boolean,
    message: String,
    isError: Boolean,
    userMessage: String,
    widthDividerMenu: Int = 2,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onMessageDismiss: () -> Unit,
    onUploadPdfClick: () -> Unit,
    selectLearningUnits: () -> Unit,
    onNextUnitClick: () -> Unit,
    onExercisesClick: () -> Unit,
    startNewChat: () -> Unit,
    deleteSubject: (String) -> Unit,
    isWebFlow: Boolean = false,
    onChangeTab: (TutorTab) -> Unit,
    selectedTab: TutorTab,
    tabInfoMessage: String,
    currentUnit: LearningUnitContentDomain,
) {
    var showErrorMessage by remember(showCustomMessage) { mutableStateOf(showCustomMessage) }
    var errorMessage by remember(message) { mutableStateOf(message) }

    val chatMessages = remember { mutableStateListOf<ChatUiMessage>() }
    var userMessageState by remember(userMessage) { mutableStateOf(userMessage) }

    LaunchedEffect(
        chatData.chat.chatSessionId,
        chatData.material.fileName,
        chatData.learningUnit.title,
        chatData.chat.messages.size
    ) {
        chatMessages.clear()

        chatMessages.addAll(
            chatData.chat.messages.map { msg ->
                ChatUiMessage(
                    sender = msg.sender,
                    text = msg.content,
                    imagePath = msg.imagePath,
                    id = msg.id
                )
            }
        )
    }

    TutorToastHost(
        showErrorMessage = showErrorMessage,
        errorMessage = errorMessage,
        isError = isError,
        onMessageDismiss = onMessageDismiss
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BoxWithConstraints {
        val drawerWidth = maxWidth / widthDividerMenu

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                TutorDrawerContent(
                    drawerWidth = drawerWidth,
                    chatHistory = chatHistory,
                    drawerState = drawerState,
                    scope = scope,
                    onSettingsClick = onSettingsClick,
                    onLogoutClick = onLogoutClick,
                    onChatClick = { selectLearningUnits() },
                    onNewChatClick = {
                        scope.launch {
                            startNewChat()
                            drawerState.close()
                        }
                    },
                    onDeleteSubject = { subjectId ->
                        deleteSubject(subjectId)
                    }
                )
            }
        ) {
            TutorScaffold(
                chatData = chatData,
                onToggleDrawer = {
                    scope.launch {
                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                    }
                },
                content = { padding ->
                    TutorChatContent(
                        padding = padding,
                        chatData = chatData,
                        chatMessages = chatMessages,
                        userMessageState = userMessageState,
                        onUserMessageChangeLocal = { userMessageState = it },
                        onExercisesClick = onExercisesClick,
                        onNextUnitClick = onNextUnitClick,
                        onUploadPdfClick = onUploadPdfClick,
                        onSendClick = {
                            TutorHandleSend(
                                userMessageState = userMessageState,
                                onClearUserMessage = { userMessageState = "" },
                                chatMessages = chatMessages.toMutableStateList(),
                            )
                        },
                        selectedTab = selectedTab,
                        onChangeTab = { onChangeTab(it) },
                        tabInfoMessage = tabInfoMessage,
                        currentUnit = currentUnit
                    )
                }
            )
        }
    }
}

@Composable
private fun TutorToastHost(
    showErrorMessage: Boolean,
    errorMessage: String,
    isError: Boolean,
    onMessageDismiss: () -> Unit,
) {
    if (showErrorMessage && errorMessage.isNotEmpty()) {
        Box(modifier = Modifier.padding(spacing24)) {
            CustomToastComposable(
                isError = isError,
                message = errorMessage,
                initializeMessage = { }
            )
        }

        LaunchedEffect(errorMessage) {
            delay(3000L)
            onMessageDismiss()
        }
    }
}

@Composable
private fun TutorDrawerContent(
    drawerWidth: Dp,
    chatHistory: ChatHistoryDomain,
    drawerState: DrawerState,
    scope: CoroutineScope,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onChatClick: (subjectId: String) -> Unit,
    onNewChatClick: () -> Unit,
    onDeleteSubject: (subjectId: String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(drawerWidth)
            .background(Color.White)
    ) {
        MenuAndChatHistory(
            chatHistory = chatHistory,
            drawerState = drawerState,
            scope = scope,
            onSettingsClick = onSettingsClick,
            onLogoutClick = onLogoutClick,
            onChatClick = onChatClick,
            onChatTitleClick = onNewChatClick,
            onDeleteSubject = onDeleteSubject
        )
    }
}

@Composable
private fun TutorScaffold(
    chatData: ChatSessionStartDomain,
    onToggleDrawer: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopBarTabs(
                material = chatData.material,
                learningUnit = chatData.learningUnit,
                changeHistoryVisibleStatus = onToggleDrawer
            )
        },
        content = content
    )
}

@Composable
private fun TutorChatContent(
    padding: PaddingValues,
    chatData: ChatSessionStartDomain,
    chatMessages: List<ChatUiMessage>,
    userMessageState: String,
    onUserMessageChangeLocal: (String) -> Unit,
    onExercisesClick: () -> Unit,
    onNextUnitClick: () -> Unit,
    onUploadPdfClick: () -> Unit,
    onSendClick: () -> Unit,
    onChangeTab: (TutorTab) -> Unit,
    selectedTab: TutorTab,
    tabInfoMessage: String,
    currentUnit: LearningUnitContentDomain,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        TutorTopActionsRow(
            fileUploaded = chatData.material.fileUploaded,
            selectedTab = selectedTab,
            onTabChange = { onChangeTab(it) },
            onNextUnitClick = onNextUnitClick
        )

        Spacer(modifier = Modifier.height(spacing8))

        when (selectedTab) {
            TutorTab.Learning -> {

                val filteredMessages =
                    if (tabInfoMessage.isBlank()) chatMessages
                    else {
                        val headerNorm = normalizeForHeaderCompare(tabInfoMessage)
                        chatMessages.filterNot { msg ->
                            msg.sender == SenderType.AI.name &&
                                    normalizeForHeaderCompare(msg.text) == headerNorm
                        }
                    }

                val config = koinInject<AppConfig>()

                LearningMessagesList(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = spacing8),
                    chatMessages = chatMessages,
                    baseUrl = config.baseUrl
                )
            }

            TutorTab.Exercises -> {
                ExercisesTab(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = spacing8),
                    exercises = currentUnit.exercises
                )
            }
        }

        if (selectedTab == TutorTab.Learning) {
            TutorInputSection(
                fileUploaded = chatData.material.fileUploaded,
                userMessageState = userMessageState,
                onUserMessageChangeLocal = onUserMessageChangeLocal,
                onSendClick = onSendClick,
                onUploadPdfClick = onUploadPdfClick
            )
        }
    }
}

@Composable
private fun TutorTopActionsRow(
    fileUploaded: Boolean,
    selectedTab: TutorTab,
    onTabChange: (TutorTab) -> Unit,
    onNextUnitClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing8),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TutorActionButton(
            text = if (selectedTab == TutorTab.Learning) "Learning ✓" else "Learning",
            enabled = fileUploaded,
            onClick = { onTabChange(TutorTab.Learning) }
        )

        TutorActionButton(
            text = if (selectedTab == TutorTab.Exercises) "Exercises ✓" else "Exercises",
            enabled = fileUploaded,
            onClick = { onTabChange(TutorTab.Exercises) }
        )

        TutorActionButton(
            text = "Next ➜",
            enabled = fileUploaded,
            onClick = onNextUnitClick
        )
    }
}

@Composable
private fun TutorActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = AppColors.Purple,
            disabledContentColor = AppColors.GrayDark
        )
    ) {
        Text(text)
    }
}

@Composable
private fun TutorInputSection(
    fileUploaded: Boolean,
    userMessageState: String,
    onUserMessageChangeLocal: (String) -> Unit,
    onSendClick: () -> Unit,
    onUploadPdfClick: () -> Unit,
) {
    ChatInputField(
        fileUploaded = fileUploaded,
        userMessage = userMessageState,
        onMessageChange = onUserMessageChangeLocal,
        onSendClick = onSendClick,
        onUploadPdfClick = onUploadPdfClick,
    )
}

@OptIn(ExperimentalUuidApi::class)
private fun TutorHandleSend(
    userMessageState: String,
    onClearUserMessage: () -> Unit,
    chatMessages: MutableList<ChatUiMessage>,
) {
    if (userMessageState.isNotBlank()) {
        chatMessages.add(
            ChatUiMessage(
                sender = SenderType.USER.name,
                text = userMessageState,
                id = Uuid.random().toString() //todo fix
            )
        )
        chatMessages.add(
            ChatUiMessage(
                sender = SenderType.AI.name,
                text = "This is a fake AI reply",
                imagePath = emptyList(),
                id = Uuid.random().toString() //todo fix
            )
        )
        onClearUserMessage()
    }
}

@Composable
fun TopBarTabs(
    material: MaterialDomain,
    learningUnit: LearningUnitDomain,
    changeHistoryVisibleStatus: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(GrayLightBoard)
            .padding(top = spacing16, start = spacing8), horizontalArrangement = Arrangement.Start
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            onClick = { changeHistoryVisibleStatus() }) {
            Icon(
                Icons.Default.Menu,
                contentDescription = "Toggle History"
            )
        }
        ChatUiHeader(
            material,
            learningUnit,
            Modifier.align(Alignment.CenterVertically).weight(1f).padding(end = spacing8)
        )
    }
}

@Composable
fun ChatUiHeader(
    material: MaterialDomain,
    learningUnit: LearningUnitDomain,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val statusFileText =
            if (material.fileUploaded) uploaded_file_success_title else uploaded_file_failure_title
        val learningText =
            if (material.fileUploaded) "\n$learning_unit_title : ${learningUnit.title}" else ""
        val titleToShow = material.fileName

        Text(
            text = "$material_title : $titleToShow - $statusFileText \n$learningText",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = spacing4).align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun ChatInputField(
    fileUploaded: Boolean,
    userMessage: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onUploadPdfClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(spacing8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (fileUploaded) {
            TextField(
                value = userMessage,
                onValueChange = onMessageChange,
                placeholder = { Text(type_your_message) },
                modifier = Modifier.weight(1f)
            )
        }
        // Upload PDF button
        if (!fileUploaded) {
            IconButton(
                onClick = onUploadPdfClick,
                enabled = !fileUploaded
            ) { Icon(Icons.Default.UploadFile, contentDescription = "Upload PDF") }
        }
        // Send
        IconButton(onClick = onSendClick) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send"
            )
        }
    }
}
