package org.example.project.myProgress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.myProgress.state.MyProgressState
import org.example.project.strings.SmartTutorStrings.all_answers
import org.example.project.strings.SmartTutorStrings.answers_left
import org.example.project.strings.SmartTutorStrings.answers_left_of_questions
import org.example.project.strings.SmartTutorStrings.attempt_title
import org.example.project.strings.SmartTutorStrings.correct_
import org.example.project.strings.SmartTutorStrings.correct_and
import org.example.project.strings.SmartTutorStrings.correct_answer_
import org.example.project.strings.SmartTutorStrings.excercise
import org.example.project.strings.SmartTutorStrings.exerscize_answer
import org.example.project.strings.SmartTutorStrings.exerscize_answers_info
import org.example.project.strings.SmartTutorStrings.exerscize_answers_sum
import org.example.project.strings.SmartTutorStrings.exerscize_sum_total
import org.example.project.strings.SmartTutorStrings.grade_info
import org.example.project.strings.SmartTutorStrings.last_answer
import org.example.project.strings.SmartTutorStrings.my_progress_title
import org.example.project.strings.SmartTutorStrings.of_which
import org.example.project.strings.SmartTutorStrings.overall_progress
import org.example.project.strings.SmartTutorStrings.progress_chat
import org.example.project.strings.SmartTutorStrings.progress_failed
import org.example.project.strings.SmartTutorStrings.progress_passed
import org.example.project.strings.SmartTutorStrings.progress_unit_not_completed
import org.example.project.strings.SmartTutorStrings.sum_answers
import org.example.project.strings.SmartTutorStrings.total_atempts_title
import org.example.project.strings.SmartTutorStrings.unit_progress_title
import org.example.project.strings.SmartTutorStrings.unit_title
import org.example.project.strings.SmartTutorStrings.wrong_
import org.example.project.strings.SmartTutorStrings.you_answered
import org.example.project.strings.SmartTutorStrings.you_answered_of
import org.example.project.strings.SmartTutorStrings.you_answered_of_answers
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
                fontWeight = FontWeight.ExtraBold,
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "$exerscize_sum_total ${state.totalCorrectAnswers + state.totalWrongAnswers} $exerscize_answer, " +
                        "$of_which ${state.totalCorrectAnswers} $correct_and ${state.totalWrongAnswers} $wrong_.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = exerscize_answers_sum,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
            )
            Spacer(Modifier.height(24.dp))

            val expandedUnits = remember { mutableStateMapOf<Int, Boolean>() }

            state.unitExerciseStats.forEach { unit ->
                val isExpanded = expandedUnits[unit.unitIndex] ?: false
                val answered = unit.totalAnswered

                val total = unit.totalExercises
                val remaining = (total - answered).coerceAtLeast(0)

                val unitProgressPercent = if (total <= 0) {
                    0
                } else {
                    (answered * 100) / total
                }


                val gradePercent = if (unit.totalMaxPoints == 0.0) {
                    0
                } else {
                    ((unit.awardedPoints * 100) / unit.totalMaxPoints).toInt()
                }

                val isCompleted = total > 0 && answered >= total

                val failed = isCompleted &&
                        unit.totalMaxPoints > 0.0 &&
                        gradePercent < 50

                val passed = isCompleted &&
                        unit.totalMaxPoints > 0.0 &&
                        gradePercent >= 50
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                            modifier = Modifier.weight(1f)
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

                    Spacer(Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "$unit_progress_title: $unitProgressPercent%",
                                fontWeight = FontWeight.Bold
                            )

                            Text("$you_answered $answered $you_answered_of $total $you_answered_of_answers")

                            Text("$answers_left $remaining $answers_left_of_questions")

                            Text(
                                text = "$grade_info : ${unit.awardedPoints}/${unit.totalMaxPoints} ($gradePercent%)",
                                color = if (failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )

                            when {
                                failed -> {
                                    Text(
                                        text = progress_failed,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                passed -> {
                                    Text(
                                        text = progress_passed,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                answered > 0 -> {
                                    Text(
                                        text = progress_unit_not_completed,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                if (isExpanded) {
                    Spacer(Modifier.height(8.dp))

                    unit.questions.forEach { question ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, bottom = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "$excercise ${question.exerciseIndex + 1}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    LearningRichMessage(
                                        text = question.question,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "$total_atempts_title ${question.attemptsCount}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        question.attempts
                                            .sortedBy { it.attemptNumber }
                                            .forEach { attempt ->
                                                Text(
                                                    text = "$attempt_title ${attempt.attemptNumber}: $grade_info ${attempt.awardedPoints}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                        Text(
                                            text = "$all_answers ${question.correctCount} $correct_ • ${question.wrongCount} $wrong_",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = last_answer,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Text(
                                        text = question.lastSelectedAnswer,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = correct_answer_,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    LearningRichMessage(
                                        text = question.correctAnswer,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
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
                modifier = Modifier.width(200.dp).fillMaxHeight()
            )

            StatBox(
                value = state.totalCorrectAnswers.toString(),
                title = correct_,
                isCorrect = true,
                modifier = Modifier.width(200.dp).fillMaxHeight()
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
                isError = true,
                modifier = Modifier.width(200.dp).fillMaxHeight()
            )

            StatBox(
                value = state.totalQuestionsAsked.toString(),
                title = progress_chat,
                modifier = Modifier.width(200.dp).fillMaxHeight()
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
