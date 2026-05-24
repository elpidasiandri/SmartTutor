package org.example.project.myProgress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.myProgress.state.MyProgressChatItem
import org.example.project.myProgress.state.MyProgressState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProgressScreen(
    state: MyProgressState,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("My Progress")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProgressOverviewCard(state)
            }

            item {
                CurrentLearningCard(state)
            }

            item {
                ActivityCard(state)
            }
            item {
                ExerciseAnswersCard(state)
            }

            item {
                Text("Recent Chats", style = MaterialTheme.typography.titleMedium)
            }

            items(state.recentChats) { chat ->
                RecentChatCard(chat)
            }
        }
    }
}

@Composable
fun ExerciseAnswersCard(state: MyProgressState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Exercise Answers", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Correct", state.totalCorrectAnswers.toString())
                StatItem("Wrong", state.totalWrongAnswers.toString())
            }

            Spacer(Modifier.height(16.dp))

            state.unitExerciseStats.forEach { unit ->
                Text(
                    text = "Unit ${unit.unitIndex + 1}: ${unit.unitTitle}",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Attempts: ${unit.totalAnswered} | Correct: ${unit.correctCount} | Wrong: ${unit.wrongCount}"
                )

                Spacer(Modifier.height(8.dp))

                unit.questions.forEach { question ->
                    Text(
                        text = "Exercise ${question.exerciseIndex + 1}",
                        fontWeight = FontWeight.Bold
                    )

                    Text(question.question)

                    Text("Attempts: ${question.attemptsCount}")
                    Text("Correct: ${question.correctCount} | Wrong: ${question.wrongCount}")
                    Text("Last answer: ${question.lastSelectedAnswer}")
                    Text("Correct answer: ${question.correctAnswer}")

                    Spacer(Modifier.height(12.dp))
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
@Composable
fun CurrentLearningCard(state: MyProgressState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Current Learning", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Text(state.currentSubjectTitle.ifBlank { "No active material" })

            Spacer(modifier = Modifier.height(8.dp))

            Text("Unit ${state.currentUnitIndex + 1} / ${state.totalUnits}")
            Text("Tab: ${state.selectedTab}")

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { state.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = ProgressIndicatorDefaults.linearColor,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )

            Text("${state.progressPercent}%")
        }
    }
}

@Composable
fun StatItem(title: String, value: String) {
    Column {
        Text(title, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun RecentChatCard(chat: MyProgressChatItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(chat.title, style = MaterialTheme.typography.titleSmall)

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { chat.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = ProgressIndicatorDefaults.linearColor,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )

            Text("${chat.progressPercent}%")
        }
    }
}

@Composable
fun ActivityCard(state: MyProgressState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Activity", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Streak", "${state.streakDays} days")
                StatItem("Last Activity", state.lastActivityLabel)
            }
        }
    }
}

@Composable
private fun ProgressOverviewCard(state: MyProgressState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Study Stats", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatItem("Chats", state.totalChats.toString())
                StatItem("PDFs", state.totalUploadedPdfs.toString())
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatItem("Questions", state.totalQuestionsAsked.toString())
                StatItem("Exercises", state.totalExercisesCount.toString())
            }
        }
    }
}
