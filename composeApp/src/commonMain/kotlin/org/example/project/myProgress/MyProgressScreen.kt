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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.example.project.myProgress.state.MyProgressState
import org.example.project.strings.SmartTutorStrings.and_
import org.example.project.strings.SmartTutorStrings.correct_
import org.example.project.strings.SmartTutorStrings.correct_and
import org.example.project.strings.SmartTutorStrings.correct_answer_
import org.example.project.strings.SmartTutorStrings.excercise
import org.example.project.strings.SmartTutorStrings.exerscize_answer
import org.example.project.strings.SmartTutorStrings.exerscize_answers_info
import org.example.project.strings.SmartTutorStrings.exerscize_answers_sum
import org.example.project.strings.SmartTutorStrings.exerscize_sum_total
import org.example.project.strings.SmartTutorStrings.last_answer
import org.example.project.strings.SmartTutorStrings.my_progress_attemps_that_u_did
import org.example.project.strings.SmartTutorStrings.my_progress_title
import org.example.project.strings.SmartTutorStrings.of_which
import org.example.project.strings.SmartTutorStrings.overall_progress
import org.example.project.strings.SmartTutorStrings.progress_chat
import org.example.project.strings.SmartTutorStrings.sum_answers
import org.example.project.strings.SmartTutorStrings.unit_title
import org.example.project.strings.SmartTutorStrings.where_
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

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, bottom = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row {
                                    Text(
                                        text = "$excercise ${question.exerciseIndex + 1}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    LearningRichMessage(
                                        text ="( ${question.question} )",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Text(
                                    text = "$my_progress_attemps_that_u_did : ${question.attemptsCount}",
                                    fontWeight = FontWeight.Medium
                                )

                                question.attempts
                                    .sortedBy { it.attemptNumber }
                                    .forEach { attempt ->
                                        Text(
                                            text = "Για την προσπάθεια ${attempt.attemptNumber}, ο βαθμός που πήρες ήταν ${attempt.awardedPoints}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                Text(
                                    text = "$you_answered ${question.attemptsCount} $you_answered_times $where_ :" +
                                            "$correct_ ${question.correctCount} $and_ $wrong_ ${question.wrongCount}",
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = last_answer,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )

                                Text(question.lastSelectedAnswer)

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = correct_answer_,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )

                                LearningRichMessage(
                                    text = question.correctAnswer,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProgressOverviewCard(state: MyProgressState) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatBox(
                value = (state.totalCorrectAnswers + state.totalWrongAnswers).toString(),
                title = sum_answers,
                modifier = Modifier.weight(1f)
            )

            StatBox(
                value = state.totalCorrectAnswers.toString(),
                title = correct_,
                isCorrect = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatBox(
                value = state.totalWrongAnswers.toString(),
                title = wrong_,
                modifier = Modifier.weight(1f),
                isError = true
            )

            StatBox(
                value = state.totalQuestionsAsked.toString(),
                title = progress_chat,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = overall_progress,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "${state.progressPercent}%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun StatBox(
    value: String,
    title: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    isCorrect: Boolean? = null,
) {
    Card(
        modifier = modifier.height(72.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else if (isCorrect == true) {
                    Color.Green
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
