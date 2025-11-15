package org.example.project.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.project.colors.AppColors.GrayVeryLight
import org.example.project.components.customToast.CustomToastComposable
import org.example.project.dimens.Dimens.spacing24
import org.example.project.strings.SmartTutorStrings.exercises_title
import org.example.project.strings.SmartTutorStrings.go_on_button
import org.example.project.strings.SmartTutorStrings.learning_units
import org.example.project.strings.SmartTutorStrings.tutor_title
import org.example.project.strings.SmartTutorStrings.type_your_message
import org.example.project.tutor.menu.MenuAndChatHistory
import org.example.project.utils.UtilsComposable.noRippleClickable

@Composable
fun TutorChatUI(
    showCustomMessage: Boolean,
    message: String,
    isError: Boolean,
    userMessage: String,
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
    var userMessageState by remember { mutableStateOf(userMessage) }
    val chatMessages = remember {
        mutableStateListOf(
            Pair("AI", "Hello! How can I help you today?"),
            Pair("User", "Hi! Can you give me an example?"),
            Pair("User", "Hi! Can you give me an example?"),
            Pair("User", "Hi! Can you give me an example?"),
            Pair("User", "Hi! Can you give me an example?"),
            Pair("User", "Hi! Can you give me an example?"),
            Pair("User", "Hi! Can you give me an example?"),
            Pair("User", "Hi! Can you give me an example?"),
            Pair("User", "Hi! Can you give me an example?"),
            Pair("User", "Hi! Can you give me an example?"),
            Pair("User", "Hi! Can you give me an example?"),
            Pair("User", "Hi! Can you give me an example?"),
        )
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

        val drawerWidth = maxWidth / 2

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
                        chatMessages = chatMessages,
                        onClose = { scope.launch { drawerState.close() } },
                        onSettingsClick = { onSettingsClick() },
                        onLogoutClick = { onLogoutClick() }
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
                        ChatMessagesList(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState()),
                            chatMessages = chatMessages,
                            onPlayClick = { message ->
                                playAIMessage(message)
                            }
                        )

                        ChatInputField(
                            userMessage = userMessageState,
                            onMessageChange = { userMessageState = it },
                            onSendClick = {
                                if (userMessageState.isNotBlank()) {
                                    chatMessages.add(Pair("User", userMessageState))
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
                                        onMessageChange(result)
                                        isMicOn = false
                                    }
                                    isMicOn = true
                                } else {
                                    stopVoiceInput()
                                    isMicOn = false
                                }
                            },
                            isMicOn = isMicOn
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun ChatInputField(
    userMessage: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onMicClick: () -> Unit,
    isMicOn: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = userMessage,
            onValueChange = onMessageChange,
            placeholder = { Text("Type your message...") },
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { onMicClick() }) {
            Icon(Icons.Default.MicOff, contentDescription = "Mic Off")
        }
        IconButton(onClick = onSendClick) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
        }
    }
}

@Composable
fun ChatMessagesList(
    modifier: Modifier,
    chatMessages: List<Pair<String, String>>,
    onPlayClick: (String) -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        chatMessages.forEach { (sender, message) ->
            if (sender == "User") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.background(Color(0xFFD1FFD6), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
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
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = message,
                        modifier = Modifier.background(Color(0xFFE8E8E8), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
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
            .background(Color(0xFFEFEFEF))
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            onClick = { changeHistoryVisibleStatus() }
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Toggle History")
        }
        TabCard(
            modifier = Modifier.weight(1f).noRippleClickable { onTabSelected("Go On") },
            "Go On",
            selectedTab == "Go On" +
                    ""
        )
        TabCard(
            modifier = Modifier.weight(1f).noRippleClickable { onTabSelected("Learning Units") },
            "Learning Units",
            selectedTab == "Learning Units"
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
        shadowElevation = if (selected) 4.dp else 0.dp
    ) {
        Column(content = {
            Text(
                text = label,
                color = if (selected) Color.White else Color.Black,
                modifier = Modifier.padding(12.dp),
                textAlign = TextAlign.Center
            )
        })
    }
}

@Composable
fun TutorComposable() {
    var exerciseType by remember { mutableStateOf<String?>(null) } // null ή τύπος άσκησης

    var userMessage by remember { mutableStateOf("") }
    val aiResponse = "AI Response will appear here"
    // Fake chat messages
    val chatMessages = remember {
        mutableStateListOf(
            Pair("AI", "Hello! How can I help you today?"),
            Pair("User", "Hi! Can you give me an example?")
        )
    }
    //TODO USE aiResponse


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        HeadersButton()
        ExerciseOptions(exerciseType)

        // Chat area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, Color.Gray)
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            chatMessages.forEach { (sender, message) ->
                if (sender == "User") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier
                                .background(GrayVeryLight, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.MicOff, contentDescription = "Mic Off")
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "AI Speaker")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = message,
                            modifier = Modifier
                                .background(Color.LightGray, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            TextField(
                value = userMessage,
                onValueChange = { userMessage = it },
                placeholder = { Text(type_your_message) },
                modifier = Modifier.weight(1f),
                maxLines = 10
            )
            IconButton(onClick = {
                if (userMessage.isNotBlank()) {
                    chatMessages.add(Pair("User", userMessage))
                    chatMessages.add(Pair("AI", "This is a fake AI reply"))
                    userMessage = ""
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
            IconButton(onClick = { /* mute/unmute microphone */ }) {
                Icon(Icons.Default.MicOff, contentDescription = "Mic Off")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Time: 00:00",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.End)
        )
    }
}


@Composable
private fun ExerciseOptions(exerciseType: String?) {
    if (exerciseType != null) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { /* T/F */ }) { Text("True/False") }
            Button(onClick = { /* MC */ }) { Text("Multiple Choice") }
            Button(onClick = { /* Development */ }) { Text("Development") }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HeadersButton() {
    var mode by remember { mutableStateOf("initial") } // "initial"  "learning"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (mode == "initial") {
            Button(onClick = { mode = "learning" }) { Text(go_on_button) }
            Button(onClick = { /* Learning Units */ }) { Text(learning_units) }
        } else if (mode == "learning") {
            Button(onClick = { mode = "initial" }) { Text(tutor_title) }
            Button(onClick = { /* Exercises */ }) { Text(exercises_title) }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}
