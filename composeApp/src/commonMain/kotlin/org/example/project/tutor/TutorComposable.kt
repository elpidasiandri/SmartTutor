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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import org.example.project.tutor.menu.MenuAndChatHistory

@Composable
fun TutorScreen(
    chatData: ChatSessionStartDomain,
    chatHistory: ChatHistoryDomain,
    showCustomMessage: Boolean,
    message: String,
    isError: Boolean,
    userMessage: String,
    widthDividerMenu: Int = 2,
    onMessageChange: (String) -> Unit,
    playAIMessage: (String) -> Unit,
    startVoiceInput: (onResult: (String) -> Unit) -> Unit,
    stopVoiceInput: () -> Unit,
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
            onMessageChange = { p1 ->
                onMessageChange(p1)
            },
            widthDividerMenu = widthDividerMenu,
            playAIMessage = playAIMessage,
            startVoiceInput = startVoiceInput,
            stopVoiceInput = stopVoiceInput,
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
    onMessageChange: (String) -> Unit,
    playAIMessage: (String) -> Unit,
    startVoiceInput: (onResult: (String) -> Unit) -> Unit,
    stopVoiceInput: () -> Unit,
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

    var isMicOn by remember { mutableStateOf(false) }
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
                    imagePath = msg.imagePath
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
                        isMicOn = isMicOn,
                        userMessageState = userMessageState,
                        onUserMessageChangeLocal = { userMessageState = it },
                        onPlayClick = playAIMessage,
                        onExercisesClick = onExercisesClick,
                        onNextUnitClick = onNextUnitClick,
                        onUploadPdfClick = onUploadPdfClick,
                        onStopVoiceInput = {
                            stopVoiceInput()
                            isMicOn = false
                        },
                        onStartVoiceInput = { onResult ->
                            startVoiceInput { result ->
                                onMessageChange(result)
                                isMicOn = false
                                onResult(result)
                            }
                            isMicOn = true
                        },
                        onSendClick = {
                            TutorHandleSend(
                                userMessageState = userMessageState,
                                onClearUserMessage = { userMessageState = "" },
                                chatMessages = chatMessages,
                                isMicOn = isMicOn,
                                stopVoiceInput = stopVoiceInput,
                                setMicOn = { isMicOn = it }
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
                chatData.material,
                chatData.learningUnit,
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
    isMicOn: Boolean,
    userMessageState: String,
    onUserMessageChangeLocal: (String) -> Unit,
    onPlayClick: (String) -> Unit,
    onExercisesClick: () -> Unit,
    onNextUnitClick: () -> Unit,
    onUploadPdfClick: () -> Unit,
    onStopVoiceInput: () -> Unit,
    onStartVoiceInput: (onResult: (String) -> Unit) -> Unit,
    onSendClick: () -> Unit,
    onChangeTab: (TutorTab) -> Unit,
    selectedTab: TutorTab,
    tabInfoMessage: String,
    currentUnit: LearningUnitContentDomain
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
                val info = tabInfoMessage.takeIf { it.isNotBlank() }
                val combined = buildList {
                    if (info != null) {
                        add(
                            ChatUiMessage(
                                sender = SenderType.AI.name,
                                text = info,
                                imagePath = emptyList()
                            )
                        )
                    }
                    addAll(chatMessages)
                }

                ChatMessagesList(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = spacing8),
                    chatMessages = combined,
                    onPlayClick = onPlayClick
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
                isMicOn = isMicOn,
                onSendClick = onSendClick,
                onMicClick = {
                    if (!isMicOn) {
                        onStartVoiceInput { result ->
                            onUserMessageChangeLocal(result)
                        }
                    } else {
                        onStopVoiceInput()
                    }
                },
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
    isMicOn: Boolean,
    onSendClick: () -> Unit,
    onMicClick: () -> Unit,
    onUploadPdfClick: () -> Unit,
) {
    ChatInputField(
        fileUploaded = fileUploaded,
        userMessage = userMessageState,
        onMessageChange = onUserMessageChangeLocal,
        onSendClick = onSendClick,
        onMicClick = onMicClick,
        onUploadPdfClick = onUploadPdfClick,
        isMicOn = isMicOn
    )
}

private fun TutorHandleSend(
    userMessageState: String,
    onClearUserMessage: () -> Unit,
    chatMessages: MutableList<ChatUiMessage>,
    isMicOn: Boolean,
    stopVoiceInput: () -> Unit,
    setMicOn: (Boolean) -> Unit,
) {
    if (userMessageState.isNotBlank()) {
        chatMessages.add(
            ChatUiMessage(
                sender = SenderType.USER.name,
                text = userMessageState
            )
        )
        chatMessages.add(
            ChatUiMessage(
                sender = SenderType.AI.name,
                text = "This is a fake AI reply",
                imagePath = emptyList()
            )
        )
        onClearUserMessage()
    }
    if (isMicOn) {
        stopVoiceInput()
        setMicOn(false)
    }
}

@Composable
fun ChatMessagesList(
    modifier: Modifier,
    chatMessages: List<ChatUiMessage>,
    onPlayClick: (String) -> Unit,
) {

    val listState = rememberLazyListState()
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }
    LazyColumn(modifier = modifier, state = listState) {
        items(chatMessages) { item ->

            if (item.sender == SenderType.USER.name) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.text,
                        modifier = Modifier.background(Color(0xFFD1FFD6), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onPlayClick(item.text) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Play AI"
                        )
                    }
                    Column(
                        modifier = Modifier
                            .background(Color(0xFFE8E8E8), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        AiRichMessageWithCode(
                            text = item.text
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(spacing8))
        }
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
fun ChatUiHeader(material: MaterialDomain, learningUnit: LearningUnitDomain, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val statusFileText =
            if (material.fileUploaded) uploaded_file_success_title else uploaded_file_failure_title
        val learningText =
            if (material.fileUploaded) "\n$learning_unit_title : ${learningUnit.title}" else ""
        Text(
            text =
                "$material_title : ${material.fileName} - $statusFileText \n" + learningText,
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
    onMicClick: () -> Unit,
    onUploadPdfClick: () -> Unit,
    isMicOn: Boolean,
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
        if (fileUploaded) {
            // Mic
            IconButton(onClick = onMicClick) {
                Icon(
                    if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Mic"
                )
            }
        } // Send
        IconButton(onClick = onSendClick) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send"
            )
        }
    }
}
