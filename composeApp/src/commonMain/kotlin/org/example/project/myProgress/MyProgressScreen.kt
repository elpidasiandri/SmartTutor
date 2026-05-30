package org.example.project.myProgress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import org.example.project.strings.SmartTutorStrings.correct_answer
import org.example.project.strings.SmartTutorStrings.correct_answer_
import org.example.project.strings.SmartTutorStrings.current_learning
import org.example.project.strings.SmartTutorStrings.excercise
import org.example.project.strings.SmartTutorStrings.exerscize_answers
import org.example.project.strings.SmartTutorStrings.last_answer
import org.example.project.strings.SmartTutorStrings.my_progress_title
import org.example.project.strings.SmartTutorStrings.progress_activity
import org.example.project.strings.SmartTutorStrings.progress_attempts
import org.example.project.strings.SmartTutorStrings.progress_chat
import org.example.project.strings.SmartTutorStrings.progress_exercises
import org.example.project.strings.SmartTutorStrings.progress_last_activity
import org.example.project.strings.SmartTutorStrings.progress_question
import org.example.project.strings.SmartTutorStrings.progress_streak
import org.example.project.strings.SmartTutorStrings.progress_streak_days
import org.example.project.strings.SmartTutorStrings.recent_chats
import org.example.project.strings.SmartTutorStrings.study_stats
import org.example.project.strings.SmartTutorStrings.unit_title
import org.example.project.strings.SmartTutorStrings.wrong_answer

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
                    Text(my_progress_title)
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
                ActivityCard(state)
            }
            item {
                ExerciseAnswersCard(state)
            }
        }
    }
}

@Composable
fun ExerciseAnswersCard(state: MyProgressState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(exerscize_answers, style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                StatItem(correct_answer, state.totalCorrectAnswers.toString())
                Spacer(modifier = Modifier.padding(start = 24.dp))
                StatItem(wrong_answer, state.totalWrongAnswers.toString())
            }

            Spacer(Modifier.height(16.dp))

            state.unitExerciseStats.forEach { unit ->
                Text(
                    text = "$unit_title ${unit.unitIndex + 1}: ${unit.unitTitle}",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$progress_attempts: ${unit.totalAnswered} | $correct_answer: ${unit.correctCount} | $wrong_answer: ${unit.wrongCount}"
                )

                Spacer(Modifier.height(8.dp))

                unit.questions.forEach { question ->
                    Text(
                        text = "$excercise ${question.exerciseIndex + 1}",
                        fontWeight = FontWeight.Bold
                    )

                    Text(question.question)

                    Text("$progress_attempts: ${question.attemptsCount}")
                    Text("$correct_answer: ${question.correctCount} | $wrong_answer: ${question.wrongCount}")
                    Text("$last_answer: ${question.lastSelectedAnswer}")
                    Text("$correct_answer_: ${question.correctAnswer}")

                    Spacer(Modifier.height(12.dp))
                }

                Spacer(Modifier.height(16.dp))
            }
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
            Text(progress_activity, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(progress_streak, "${state.streakDays} $progress_streak_days")
                StatItem(progress_last_activity, state.lastActivityLabel)
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
            Text(study_stats, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatItem(progress_chat, state.totalQuestionsAsked.toString())
                Spacer(modifier = Modifier.width(24.dp))

                StatItem("PDFs", state.totalUploadedPdfs.toString())
            }

        }
    }
}
