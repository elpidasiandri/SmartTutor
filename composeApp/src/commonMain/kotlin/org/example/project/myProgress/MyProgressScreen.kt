package org.example.project.myProgress

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.myProgress.state.MyProgressState
import org.example.project.strings.SmartTutorStrings.correct_and
import org.example.project.strings.SmartTutorStrings.correct_answer_
import org.example.project.strings.SmartTutorStrings.excercise
import org.example.project.strings.SmartTutorStrings.exerscize_answer
import org.example.project.strings.SmartTutorStrings.exerscize_answers_info
import org.example.project.strings.SmartTutorStrings.exerscize_answers_sum
import org.example.project.strings.SmartTutorStrings.exerscize_sum_total
import org.example.project.strings.SmartTutorStrings.last_answer
import org.example.project.strings.SmartTutorStrings.my_progress_title
import org.example.project.strings.SmartTutorStrings.of_which
import org.example.project.strings.SmartTutorStrings.overall_progress
import org.example.project.strings.SmartTutorStrings.progress_chat
import org.example.project.strings.SmartTutorStrings.study_stats
import org.example.project.strings.SmartTutorStrings.unit_title
import org.example.project.strings.SmartTutorStrings.wrong_
import org.example.project.strings.SmartTutorStrings.you_answered
import org.example.project.strings.SmartTutorStrings.you_answered_times
import org.example.project.tutor.learningList.LearningRichMessage

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
                if (state.totalCorrectAnswers + state.totalWrongAnswers > 0) {
                    ExerciseAnswersCard(state)
                }
            }
        }
    }
}

@Composable
fun ExerciseAnswersCard(state: MyProgressState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = exerscize_answers_info,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "$exerscize_sum_total ${state.totalCorrectAnswers + state.totalWrongAnswers} $exerscize_answer, " +
                        "$of_which ${state.totalCorrectAnswers} $correct_and ${state.totalWrongAnswers} $wrong_.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = exerscize_answers_sum,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal
            )
            Spacer(Modifier.height(24.dp))

            val expandedUnits = remember { mutableStateMapOf<Int, Boolean>() }

            state.unitExerciseStats.forEach { unit ->
                val isExpanded = expandedUnits[unit.unitIndex] ?: false

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedUnits[unit.unitIndex] = !isExpanded
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$unit_title ${unit.unitIndex + 1}: ${unit.unitTitle}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                    )

                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Filled.KeyboardArrowDown
                        } else {
                            Icons.AutoMirrored.Filled.KeyboardArrowRight
                        },
                        contentDescription = null
                    )
                }

                if (isExpanded) {
                    Spacer(Modifier.height(8.dp))

                    unit.questions.forEach { question ->
                        Column(
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            Text(
                                text = "$excercise ${question.exerciseIndex + 1}",
                                fontWeight = FontWeight.Bold
                            )

                            Text(question.question)

                            Text(
                                text = "$you_answered ${question.attemptsCount} $you_answered_times: " +
                                        "${question.correctCount} $correct_and ${question.wrongCount} $wrong_."
                            )

                            Text("$last_answer: ${question.lastSelectedAnswer}")

                            LearningRichMessage(
                                text = "$correct_answer_: ${question.correctAnswer}",
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(12.dp))
                        }
                    }
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
private fun ProgressOverviewCard(state: MyProgressState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                study_stats,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatItem(progress_chat, state.totalQuestionsAsked.toString())
                Spacer(modifier = Modifier.width(24.dp))

            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$overall_progress${state.progressPercent}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LinearProgressIndicator(
                progress = { state.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}
