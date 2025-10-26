package org.example.project.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.colors.AppColors.GrayVeryLight
import org.example.project.strings.SmartTutorStrings.exercises_title
import org.example.project.strings.SmartTutorStrings.go_on_button
import org.example.project.strings.SmartTutorStrings.learning_units
import org.example.project.strings.SmartTutorStrings.tutor_title
import org.example.project.strings.SmartTutorStrings.type_your_message

@Composable
fun TutorComposable() {
    var exerciseType by remember { mutableStateOf<String?>(null) } // null ή τύπος άσκησης

    var userMessage by remember { mutableStateOf("") }
    val aiResponse = "AI Response will appear here"
    // Fake chat messages
    val chatMessages = remember { mutableStateListOf(
        Pair("AI", "Hello! How can I help you today?"),
        Pair("User", "Hi! Can you give me an example?")
    ) }
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
private fun HeadersButton(){
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
