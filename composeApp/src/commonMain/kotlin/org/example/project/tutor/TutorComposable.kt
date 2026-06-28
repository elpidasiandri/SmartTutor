package org.example.project.tutor

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import org.example.project.AppConfig
import org.example.project.colors.AppColors
import org.example.project.colors.AppColors.GrayLightBoard
import org.example.project.components.customToast.CustomToastComposable
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing24
import org.example.project.dimens.Dimens.spacing4
import org.example.project.dimens.Dimens.spacing8
import org.example.project.dimens.Dimens.spacing80
import org.example.project.strings.SmartTutorStrings.exercises_tab
import org.example.project.strings.SmartTutorStrings.learning_tab
import org.example.project.strings.SmartTutorStrings.learning_unit_title
import org.example.project.strings.SmartTutorStrings.material_title
import org.example.project.strings.SmartTutorStrings.next_tab
import org.example.project.strings.SmartTutorStrings.type_your_message
import org.example.project.strings.SmartTutorStrings.uploaded_file_failure_title
import org.example.project.strings.SmartTutorStrings.uploaded_file_success_title
import org.example.project.strings.SmartTutorStrings.waiting_for_answer
import org.example.project.strings.SmartTutorStrings.waiting_for_response
import org.example.project.tutor.models.domainModels.chatHistory.ChatHistoryDomain
import org.example.project.tutor.models.domainModels.learningUnits.LearningUnitContentDomain
import org.example.project.tutor.models.domainModels.tutorChat.ChatSessionStartDomain
import org.example.project.tutor.models.domainModels.tutorChat.ChatUiMessage
import org.example.project.tutor.models.domainModels.tutorChat.LearningUnitDomain
import org.example.project.tutor.models.domainModels.tutorChat.MaterialDomain
import org.example.project.tutor.models.domainModels.tutorChat.TutorTab
import org.example.project.tutor.excercise.ExercisesTab
import org.example.project.tutor.learningList.LearningMessagesList
import org.example.project.tutor.menu.MenuAndChatHistory
import org.example.project.tutor.models.domainModels.learningUnits.ExerciseType
import org.koin.compose.koinInject
import kotlin.String
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.clickable

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
    selectLearningUnit: (String, String) -> Unit,
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
    onSendMessage: (String) -> Unit,
    openExistingChat: (String) -> Unit,
    navigateToMyProgress: () -> Unit,
    canGoNext: Boolean,
    askTutor: Boolean,
    onExerciseAnswered: (Int, ExerciseType, String, String, Boolean, Long, String, Double, Double, String) -> Unit,
    onAskTutorForOpenExercise: (
        exerciseIndex: Int,
        question: String,
        userAnswer: String,
        suggestedSolution: String,
    ) -> Unit,
    exerciseTutorAnswers: Map<Int, String>,
    answeredExerciseKeys: Set<String>,
    currentIndex: Int,
    selectedExerciseIndex: Int,
    onMoreExercisesClick: () -> Unit,
    isLoadingMoreExercises: Boolean,
    onLearningClick: (String, String) -> Unit,
    onExercisesMenuTabClick: (String, String, Int) -> Unit,
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
            selectLearningUnit = { title, subjectId ->
                selectLearningUnit(title, subjectId)
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
            onChangeTab = { onChangeTab(it) },
            selectedTab = selectedTab,
            tabInfoMessage = tabInfoMessage,
            currentUnit = currentUnit,
            onSendMessage = { text ->
                onSendMessage(text)
            },
            isLoading = isLoading,
            onExistingChatClick = { chatId ->
                openExistingChat(chatId)
            },
            navigateToMyProgress = {
                navigateToMyProgress()
            },
            canGoNext = canGoNext,
            askTutor = askTutor,
            onExerciseAnswered = onExerciseAnswered,
            onAskTutorForOpenExercise = onAskTutorForOpenExercise,
            exerciseTutorAnswers = exerciseTutorAnswers,
            answeredExerciseKeys = answeredExerciseKeys,
            currentIndex = currentIndex,
            selectedExerciseIndex = selectedExerciseIndex,
            onMoreExercisesClick = onMoreExercisesClick,
            isLoadingMoreExercises = isLoadingMoreExercises,
            onLearningClick = { title, subjectId ->
                onLearningClick(title, subjectId)
            },
            onExercisesMenuTabClick = { title, subjectId, index ->
                onExercisesMenuTabClick(title, subjectId, index)
            },
        )
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
    selectLearningUnit: (String, String) -> Unit,
    onNextUnitClick: () -> Unit,
    onExercisesClick: () -> Unit,
    startNewChat: () -> Unit,
    deleteSubject: (String) -> Unit,
    isWebFlow: Boolean = false,
    onChangeTab: (TutorTab) -> Unit,
    selectedTab: TutorTab,
    tabInfoMessage: String,
    currentUnit: LearningUnitContentDomain,
    onSendMessage: (String) -> Unit,
    isLoading: Boolean,
    onExistingChatClick: (String) -> Unit,
    navigateToMyProgress: () -> Unit,
    canGoNext: Boolean,
    askTutor: Boolean,
    onExerciseAnswered: (Int, ExerciseType, String, String, Boolean, Long, String, Double, Double, String) -> Unit,
    onAskTutorForOpenExercise: (
        exerciseIndex: Int,
        question: String,
        userAnswer: String,
        suggestedSolution: String,
    ) -> Unit,
    exerciseTutorAnswers: Map<Int, String>,
    answeredExerciseKeys: Set<String>,
    currentIndex: Int,
    selectedExerciseIndex: Int,
    onMoreExercisesClick: () -> Unit,
    isLoadingMoreExercises: Boolean,
    onLearningClick: (String, String) -> Unit,
    onExercisesMenuTabClick: (String, String, Int) -> Unit,
) {
    var showErrorMessage by remember(showCustomMessage) { mutableStateOf(showCustomMessage) }
    var errorMessage by remember(message) { mutableStateOf(message) }

    val chatMessages = remember { mutableStateListOf<ChatUiMessage>() }
    var userMessageState by remember(userMessage) { mutableStateOf(userMessage) }
    var isMobileMenuOpen by remember { mutableStateOf(false) }

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
                    id = msg.id,
                    timestamp = msg.timestamp
                )
            }
        )
    }

    BoxWithConstraints {
        val isMobile = maxWidth < 600.dp

        val drawerWidth = when {
            isMobile -> maxWidth * 0.86f
            maxWidth < 900.dp -> 340.dp
            else -> 380.dp
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize()) {

                if (!isMobile) {
                    TutorDrawerContent(
                        drawerWidth = drawerWidth,
                        chatHistory = chatHistory,
                        onSettingsClick = onSettingsClick,
                        onLogoutClick = onLogoutClick,
                        onLearningUnitClick = { title, subjectId ->
                            selectLearningUnit(title, subjectId)
                        },
                        onNewChatClick = startNewChat,
                        onDeleteSubject = deleteSubject,
                        onExistingChatClick = onExistingChatClick,
                        navigateToMyProgress = navigateToMyProgress,
                        onLearningClick = { title, subjectId ->
                            onLearningClick(title, subjectId)
                        },
                        onExercisesClick = { title, subjectId, index ->
                            onExercisesMenuTabClick(title, subjectId, index)
                        },
                        answeredExerciseKeys = answeredExerciseKeys,
                        currentIndex = currentIndex,
                        selectedTab = selectedTab,
                        selectedExerciseIndex = selectedExerciseIndex,
                        activeChatId = chatData.chat.chatSessionId,
                    )
                }

                TutorScaffold(
                    chatData = chatData,
                    content = { padding ->
                        Box(modifier = Modifier.fillMaxSize()) {
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
                                    val text = userMessageState.trim()
                                    if (text.isNotBlank()) {
                                        onSendMessage(text)
                                        userMessageState = ""
                                    }
                                },
                                selectedTab = selectedTab,
                                onChangeTab = onChangeTab,
                                tabInfoMessage = tabInfoMessage,
                                currentUnit = currentUnit,
                                isLoading = isLoading,
                                canGoNext = canGoNext,
                                onExerciseAnswered = onExerciseAnswered,
                                onAskTutorForOpenExercise = onAskTutorForOpenExercise,
                                exerciseTutorAnswers = exerciseTutorAnswers,
                                answeredExerciseKeys = answeredExerciseKeys,
                                currentIndex = currentIndex,
                                onMoreExercisesClick = onMoreExercisesClick,
                                isLoadingMoreExercises = isLoadingMoreExercises,
                                selectedExerciseIndex = selectedExerciseIndex,
                            )

                            if (isMobile) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(top = 90.dp, start = 8.dp)
                                        .zIndex(9999f)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(50)
                                        )
                                ) {
                                    IconButton(
                                        onClick = { isMobileMenuOpen = true }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Open menu"
                                        )
                                    }
                                }
                            }

                            TutorToastHost(
                                showErrorMessage = showErrorMessage,
                                errorMessage = errorMessage,
                                isError = isError,
                                onMessageDismiss = onMessageDismiss
                            )
                        }

                        if (isLoading || askTutor) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zIndex(999f)
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .pointerInput(Unit) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                awaitPointerEvent()
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(waiting_for_response, color = Color.White)
                                }
                            }
                        }
                    }
                )
            }

            if (isMobile && isMobileMenuOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(100f)
                        .background(Color.Black.copy(alpha = 0.35f))
                        .clickable { isMobileMenuOpen = false }
                )

                Box(
                    modifier = Modifier
                        .width(drawerWidth)
                        .fillMaxHeight()
                        .zIndex(101f)
                        .background(Color.White)
                ) {
                    TutorDrawerContent(
                        drawerWidth = drawerWidth,
                        chatHistory = chatHistory,
                        onSettingsClick = {
                            isMobileMenuOpen = false
                            onSettingsClick()
                        },
                        onLogoutClick = {
                            isMobileMenuOpen = false
                            onLogoutClick()
                        },
                        onLearningUnitClick = { title, subjectId ->
                            isMobileMenuOpen = false
                            selectLearningUnit(title, subjectId)
                        },
                        onNewChatClick = {
                            isMobileMenuOpen = false
                            startNewChat()
                        },
                        onDeleteSubject = deleteSubject,
                        onExistingChatClick = { chatId ->
                            isMobileMenuOpen = false
                            onExistingChatClick(chatId)
                        },
                        navigateToMyProgress = {
                            isMobileMenuOpen = false
                            navigateToMyProgress()
                        },
                        onLearningClick = { title, subjectId ->
                            isMobileMenuOpen = false
                            onLearningClick(title, subjectId)
                        },
                        onExercisesClick = { title, subjectId, index ->
                            isMobileMenuOpen = false
                            onExercisesMenuTabClick(title, subjectId, index)
                        },
                        answeredExerciseKeys = answeredExerciseKeys,
                        currentIndex = currentIndex,
                        selectedTab = selectedTab,
                        selectedExerciseIndex = selectedExerciseIndex,
                        activeChatId = chatData.chat.chatSessionId,
                    )
                }
            }
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
        Box(
            modifier = Modifier.padding(
                start = spacing24,
                bottom = spacing80,
                end = spacing24
            )
        ) {

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
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onLearningUnitClick: (String, String) -> Unit,
    onNewChatClick: () -> Unit,
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
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(drawerWidth)
            .background(Color.White)
    ) {
        MenuAndChatHistory(
            chatHistory = chatHistory,
            onSettingsClick = onSettingsClick,
            onLogoutClick = onLogoutClick,
            onLearningUnitClick = onLearningUnitClick,
            onChatTitleClick = onNewChatClick,
            onDeleteSubject = onDeleteSubject,
            onExistingChatClick = { chatId ->
                onExistingChatClick(chatId)
            },
            navigateToMyProgress = {
                navigateToMyProgress()
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
    }
}

@Composable
private fun TutorScaffold(
    chatData: ChatSessionStartDomain,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopBarTabs(
                material = chatData.material,
                learningUnit = chatData.learningUnit,
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
    isLoading: Boolean,
    canGoNext: Boolean,
    onExerciseAnswered: (Int, ExerciseType, String, String, Boolean, Long, String, Double, Double, String) -> Unit,
    onAskTutorForOpenExercise: (
        exerciseIndex: Int,
        question: String,
        userAnswer: String,
        suggestedSolution: String,
    ) -> Unit,
    exerciseTutorAnswers: Map<Int, String>,
    answeredExerciseKeys: Set<String>,
    currentIndex: Int,
    selectedExerciseIndex: Int,
    onMoreExercisesClick: () -> Unit,
    isLoadingMoreExercises: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing8),
            horizontalArrangement = Arrangement.End
        ) {
            TutorActionButton(
                text = next_tab,
                enabled = chatData.material.fileUploaded && !isLoading,
                onClick = onNextUnitClick,
                selectedTab = false
            )
        }
        Spacer(modifier = Modifier.height(spacing8))

        when (selectedTab) {
            TutorTab.Learning -> {

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
                    exercises = currentUnit.exercises,
                    selectedExerciseIndex = selectedExerciseIndex,
                    onExerciseAnswered = onExerciseAnswered,
                    onAskTutorForOpenExercise = onAskTutorForOpenExercise,
                    exerciseTutorAnswers = exerciseTutorAnswers,
                    answeredExerciseKeys = answeredExerciseKeys,
                    currentUnitIndex = currentIndex,
                    onMoreExercisesClick = onMoreExercisesClick,
                    isLoadingMoreExercises = isLoadingMoreExercises,
                )
            }
        }

        if (selectedTab == TutorTab.Learning) {
            TutorInputSection(
                fileUploaded = chatData.material.fileUploaded,
                userMessageState = userMessageState,
                onUserMessageChangeLocal = onUserMessageChangeLocal,
                onSendClick = onSendClick,
                onUploadPdfClick = onUploadPdfClick,
                isLoading = isLoading
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
    isLoading: Boolean,
    canGoNext: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing8),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TutorActionButton(
            text = learning_tab,
            enabled = fileUploaded && !isLoading,
            onClick = { onTabChange(TutorTab.Learning) },
            selectedTab = selectedTab == TutorTab.Learning,
        )

        TutorActionButton(
            text = exercises_tab,
            enabled = fileUploaded && !isLoading,
            onClick = { onTabChange(TutorTab.Exercises) },
            selectedTab = selectedTab == TutorTab.Exercises,
        )

        TutorActionButton(
            text = next_tab,
            enabled = fileUploaded && !isLoading && canGoNext,
            onClick = onNextUnitClick,
            selectedTab = false,
        )
    }
}

@Composable
private fun TutorActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    selectedTab: Boolean,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selectedTab) AppColors.Purple else Color.White,
            contentColor = if (selectedTab) Color.White else AppColors.Purple,
            disabledContainerColor = AppColors.GrayLight,
            disabledContentColor = AppColors.GrayDark
        ),
        border = BorderStroke(1.dp, AppColors.Purple)
    ) {
        Text(
            text = text,
            fontWeight = if (selectedTab) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun TutorInputSection(
    fileUploaded: Boolean,
    userMessageState: String,
    onUserMessageChangeLocal: (String) -> Unit,
    onSendClick: () -> Unit,
    onUploadPdfClick: () -> Unit,
    isLoading: Boolean,
) {
    ChatInputField(
        fileUploaded = fileUploaded,
        userMessage = userMessageState,
        onMessageChange = onUserMessageChangeLocal,
        onSendClick = onSendClick,
        onUploadPdfClick = onUploadPdfClick,
        isLoading = isLoading,
    )
}

@Composable
fun TopBarTabs(
    material: MaterialDomain,
    learningUnit: LearningUnitDomain,
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(GrayLightBoard)
            .padding(top = spacing16, start = spacing8), horizontalArrangement = Arrangement.Start
    ) {
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
            if (material.fileUploaded) "\n$learning_unit_title ${learningUnit.orderIndex}: ${learningUnit.title}" else ""

        Text(
            text = "$material_title : $statusFileText \n$learningText",
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
    isLoading: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(spacing8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (fileUploaded) {
            TextField(
                value = userMessage,
                onValueChange = onMessageChange,
                placeholder = { Text(if (isLoading) waiting_for_answer else type_your_message) },
                modifier = Modifier.weight(1f)
            )
        }
        // Upload PDF button
        if (!fileUploaded) {
            IconButton(
                onClick = onUploadPdfClick,
                enabled = !fileUploaded && !isLoading
            ) { Icon(Icons.Default.UploadFile, contentDescription = "Upload PDF") }
        }
        // Send
        IconButton(
            onClick = onSendClick,
            enabled = !isLoading && userMessage.isNotBlank()
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send"
            )
        }
    }
}
