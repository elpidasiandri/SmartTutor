package org.example.project.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.project.colors.AppColors.GrayLightBoard
import org.example.project.components.customToast.CustomToastComposable
import org.example.project.dimens.Dimens.spacing12
import org.example.project.dimens.Dimens.spacing16
import org.example.project.dimens.Dimens.spacing24
import org.example.project.dimens.Dimens.spacing4
import org.example.project.dimens.Dimens.spacing8
import org.example.project.strings.SmartTutorStrings.go_on_button
import org.example.project.strings.SmartTutorStrings.learning_unit_title
import org.example.project.strings.SmartTutorStrings.learning_units
import org.example.project.strings.SmartTutorStrings.material_title
import org.example.project.strings.SmartTutorStrings.type_your_message
import org.example.project.strings.SmartTutorStrings.uploaded_file_failure_title
import org.example.project.strings.SmartTutorStrings.uploaded_file_success_title
import org.example.project.tutor.domainModels.chatHistory.ChatHistoryDomain
import org.example.project.tutor.domainModels.tutorChat.ChatSessionStartDomain
import org.example.project.tutor.domainModels.tutorChat.LUStatusDomain
import org.example.project.tutor.domainModels.tutorChat.LearningUnitDomain
import org.example.project.tutor.domainModels.tutorChat.MaterialDomain
import org.example.project.tutor.domainModels.tutorChat.SenderType
import org.example.project.tutor.menu.MenuAndChatHistory
import org.example.project.utils.UtilsComposable.noRippleClickable

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
    onProgressClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onMessageDismiss: () -> Unit,
) {
    var showErrorMessage by remember(showCustomMessage) { mutableStateOf(showCustomMessage) }
    var errorMessage by remember(message) { mutableStateOf(message) }

    var isMicOn by remember { mutableStateOf(false) }
    var userMessageState by remember(userMessage) { mutableStateOf(userMessage) }
    val chatMessages = remember {
        mutableStateListOf(*chatData.chat.messages.map {
            Pair(it.sender, it.content)
        }.toTypedArray())
    }

    if (showErrorMessage && errorMessage.isNotEmpty()) {
        Box(modifier = Modifier.padding(spacing24)) {
            CustomToastComposable(
                isError = isError,
                message = errorMessage,
                initializeMessage = { showErrorMessage = true }
            )
        }

        LaunchedEffect(errorMessage) {
            delay(3000L)
            onMessageDismiss()
        }
    }
    var selectedTab by remember { mutableStateOf("Go On") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BoxWithConstraints {

        val drawerWidth = maxWidth / widthDividerMenu

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
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
                        onSettingsClick = { onSettingsClick() },
                        onLogoutClick = { onLogoutClick() },
                        onChatClick = { subjectId, luId ->

                        }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopBarTabs(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        changeHistoryVisibleStatus = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open()
                                else drawerState.close()
                            }
                        }
                    )
                },
                content = { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        ChatUiHeader(chatData.material, chatData.learningUnit)


                        Spacer(modifier = Modifier.height(spacing8))

                        // -------------------- CHAT MESSAGES --------------------
                        ChatMessagesList(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = spacing8),
                            chatMessages = chatMessages,
                            onPlayClick = { message -> playAIMessage(message) }
                        )

                        // -------------------- INPUT FIELD --------------------
                        if (chatData.instance.status != LUStatusDomain.INACTIVE) {
                            ChatInputField(
                                fileUploaded = chatData.material.fileUploaded,
                                userMessage = userMessageState,
                                onMessageChange = { userMessageState = it },
                                onSendClick = {
                                    if (userMessageState.isNotBlank()) {
                                        chatMessages.add(Pair("USER", userMessageState))
                                        chatMessages.add(Pair("AI", "This is a fake AI reply"))
                                        userMessageState = ""
                                    }
                                    if (isMicOn) {
                                        stopVoiceInput()
                                        isMicOn = false
                                    }
                                },
                                onMicClick = {
                                    if (!isMicOn) {
                                        startVoiceInput { result ->
                                            println("Q12345 result compose $result")
                                            onMessageChange(result)  // ενημερώνει το Fragment
                                            isMicOn = false
                                        }
                                        isMicOn = true
                                    } else {
                                        stopVoiceInput()
                                        isMicOn = false
                                    }
                                }
                                ,
                                isMicOn = isMicOn,
                                onUploadPdfClick = {
                                    // open PDF picker
                                },
                            )
                        }
                    }
                }
            )
        }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacing8),
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextField(
            value = userMessage,
            onValueChange = onMessageChange,
            placeholder = { Text(type_your_message) },
            modifier = Modifier.weight(1f)
        )

        // Upload PDF button
        IconButton(
            onClick = onUploadPdfClick,
            enabled = !fileUploaded
        ) {
            Icon(Icons.Default.UploadFile, contentDescription = "Upload PDF")
        }

        // Mic
        IconButton(onClick = onMicClick) {
            Icon(
                if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff,
                contentDescription = "Mic"
            )
        }

        // Send
        IconButton(onClick = onSendClick) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
        }
    }
}


@Composable
fun ChatUiHeader(material: MaterialDomain, learningUnit: LearningUnitDomain) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GrayLightBoard)
            .padding(vertical = spacing12),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val statusFileText =
            if (material.fileUploaded) uploaded_file_success_title else uploaded_file_failure_title
        Text(
            text = "$material_title : ${material.fileName} - $statusFileText",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = spacing4)
        )

        Text(
            text = "$learning_unit_title : ${learningUnit.title}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun ChatMessagesList(
    modifier: Modifier,
    chatMessages: List<Pair<String, String>>,
    onPlayClick: (String) -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        items(chatMessages) { (sender, message) ->
            if (sender == SenderType.USER.name) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        modifier = Modifier
                            .background(Color(0xFFD1FFD6), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onPlayClick(message) }) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Play AI")
                    }
                    Text(
                        text = message,
                        modifier = Modifier
                            .background(Color(0xFFE8E8E8), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(spacing8))
        }
    }
}


@Composable
fun TopBarTabs(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    changeHistoryVisibleStatus: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GrayLightBoard)
            .padding(top = spacing16, start = spacing8),
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            onClick = { changeHistoryVisibleStatus() }
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Toggle History")
        }
        TabCard(
            modifier = Modifier.align(Alignment.CenterVertically).weight(1f).padding(end = spacing8)
                .noRippleClickable { onTabSelected(go_on_button) },
            go_on_button,
            selectedTab == go_on_button
        )
        TabCard(
            modifier = Modifier.align(Alignment.CenterVertically).weight(1f).padding(end = spacing8)
                .noRippleClickable { onTabSelected(learning_units) },
            learning_units,
            selectedTab == learning_units
        )

    }
}

@Composable
fun TabCard(
    modifier: Modifier,
    label: String,
    selected: Boolean,
    shape: Shape = CardDefaults.shape,
) {
    val backgroundColor = if (selected) Color.Blue else Color.LightGray
    val contentColor = if (selected) Color.White else Color.Black

    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor,
        shadowElevation = if (selected) spacing4 else 0.dp
    ) {
        Column(content = {
            Text(
                text = label,
                color = if (selected) Color.White else Color.Black,
                modifier = Modifier.padding(spacing12).align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center
            )
        })
    }
}